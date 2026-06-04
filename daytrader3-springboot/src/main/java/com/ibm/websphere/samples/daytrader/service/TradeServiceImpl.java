/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.ibm.websphere.samples.daytrader.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.websphere.samples.daytrader.TradeServices;
import com.ibm.websphere.samples.daytrader.domain.AccountDataBean;
import com.ibm.websphere.samples.daytrader.domain.AccountProfileDataBean;
import com.ibm.websphere.samples.daytrader.domain.HoldingDataBean;
import com.ibm.websphere.samples.daytrader.domain.OrderDataBean;
import com.ibm.websphere.samples.daytrader.domain.QuoteDataBean;
import com.ibm.websphere.samples.daytrader.dto.MarketSummaryDataBean;
import com.ibm.websphere.samples.daytrader.dto.RunStatsDataBean;
import com.ibm.websphere.samples.daytrader.repository.AccountProfileRepository;
import com.ibm.websphere.samples.daytrader.repository.AccountRepository;
import com.ibm.websphere.samples.daytrader.repository.HoldingRepository;
import com.ibm.websphere.samples.daytrader.repository.OrderRepository;
import com.ibm.websphere.samples.daytrader.repository.QuoteRepository;
import com.ibm.websphere.samples.daytrader.util.FinancialUtils;
import com.ibm.websphere.samples.daytrader.util.Log;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;
import com.ibm.websphere.samples.daytrader.util.TradeException;

/**
 * Spring {@code @Service} migration of the legacy stateless session bean
 * {@code TradeSLSBBean}. Container-managed transactions are replaced by Spring
 * {@link Transactional} and {@code EntityManager} access by Spring Data JPA
 * repositories. Business behavior of the synchronous trade actions is preserved.
 *
 * <p>JMS/async paths ({@link #queueOrder}, {@link #publishQuotePriceChange},
 * {@link #pingTwoPhase}) and the bulk {@link #resetTrade} JDBC path are deferred
 * from this slice and documented in the migration summary.</p>
 */
@Service
@Transactional
public class TradeServiceImpl implements TradeServices {

    private final QuoteRepository quoteRepository;
    private final AccountRepository accountRepository;
    private final AccountProfileRepository accountProfileRepository;
    private final HoldingRepository holdingRepository;
    private final OrderRepository orderRepository;

    public TradeServiceImpl(QuoteRepository quoteRepository,
            AccountRepository accountRepository,
            AccountProfileRepository accountProfileRepository,
            HoldingRepository holdingRepository,
            OrderRepository orderRepository) {
        this.quoteRepository = quoteRepository;
        this.accountRepository = accountRepository;
        this.accountProfileRepository = accountProfileRepository;
        this.holdingRepository = holdingRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public MarketSummaryDataBean getMarketSummary() {
        try {
            if (Log.doTrace()) {
                Log.trace("TradeServiceImpl:getMarketSummary -- getting market summary");
            }

            List<QuoteDataBean> quotes = quoteRepository.findQuotesByChange();
            QuoteDataBean[] quoteArray = quotes.toArray(new QuoteDataBean[quotes.size()]);
            ArrayList<QuoteDataBean> topGainers = new ArrayList<QuoteDataBean>(5);
            ArrayList<QuoteDataBean> topLosers = new ArrayList<QuoteDataBean>(5);
            BigDecimal TSIA = FinancialUtils.ZERO;
            BigDecimal openTSIA = FinancialUtils.ZERO;
            double totalVolume = 0.0;

            if (quoteArray.length > 5) {
                for (int i = 0; i < 5; i++) {
                    topGainers.add(quoteArray[i]);
                }
                for (int i = quoteArray.length - 1; i >= quoteArray.length - 5; i--) {
                    topLosers.add(quoteArray[i]);
                }

                for (QuoteDataBean quote : quoteArray) {
                    BigDecimal price = quote.getPrice();
                    BigDecimal open = quote.getOpen();
                    double volume = quote.getVolume();
                    TSIA = TSIA.add(price);
                    openTSIA = openTSIA.add(open);
                    totalVolume += volume;
                }
                TSIA = TSIA.divide(new BigDecimal(quoteArray.length), FinancialUtils.ROUND);
                openTSIA = openTSIA.divide(new BigDecimal(quoteArray.length), FinancialUtils.ROUND);
            }

            return new MarketSummaryDataBean(TSIA, openTSIA, totalVolume, topGainers, topLosers);
        } catch (Exception e) {
            Log.error("TradeServiceImpl:getMarketSummary", e);
            throw new TradeException("TradeServiceImpl:getMarketSummary -- error ", e);
        }
    }

    @Override
    public OrderDataBean buy(String userID, String symbol, double quantity, int orderProcessingMode) {
        OrderDataBean order;
        BigDecimal total;
        try {
            if (Log.doTrace()) {
                Log.trace("TradeServiceImpl:buy", userID, symbol, quantity, orderProcessingMode);
            }

            AccountProfileDataBean profile = accountProfileRepository.findById(userID).orElse(null);
            AccountDataBean account = profile.getAccount();
            QuoteDataBean quote = quoteRepository.findById(symbol).orElse(null);
            HoldingDataBean holding = null; // The holding will be created by this buy order

            order = createOrder(account, quote, holding, "buy", quantity);

            // account is debited during completeOrder via the order proceeds
            BigDecimal price = quote.getPrice();
            BigDecimal orderFee = order.getOrderFee();
            BigDecimal balance = account.getBalance();
            total = (new BigDecimal(quantity).multiply(price)).add(orderFee);
            account.setBalance(balance.subtract(total));

            if (orderProcessingMode == TradeConfig.SYNCH) {
                completeOrder(order.getOrderID(), false);
            } else if (orderProcessingMode == TradeConfig.ASYNCH_2PHASE) {
                queueOrder(order.getOrderID(), true);
            }
        } catch (Exception e) {
            Log.error("TradeServiceImpl:buy(" + userID + "," + symbol + "," + quantity + ") --> failed", e);
            throw new TradeException(e);
        }
        return order;
    }

    @Override
    public OrderDataBean sell(String userID, Integer holdingID, int orderProcessingMode) {
        OrderDataBean order;
        BigDecimal total;
        try {
            if (Log.doTrace()) {
                Log.trace("TradeServiceImpl:sell", userID, holdingID, orderProcessingMode);
            }

            AccountProfileDataBean profile = accountProfileRepository.findById(userID).orElse(null);
            AccountDataBean account = profile.getAccount();
            HoldingDataBean holding = holdingRepository.findById(holdingID).orElse(null);

            if (holding == null) {
                Log.error("TradeServiceImpl:sell User " + userID + " attempted to sell holding " + holdingID
                        + " which has already been sold");

                OrderDataBean orderData = new OrderDataBean();
                orderData.setOrderStatus("cancelled");
                orderRepository.save(orderData);

                return orderData;
            }

            QuoteDataBean quote = holding.getQuote();
            double quantity = holding.getQuantity();
            order = createOrder(account, quote, holding, "sell", quantity);

            // signify this holding is "inflight" to be sold
            holding.setPurchaseDate(new java.sql.Timestamp(0));

            // account is credited during completeOrder
            BigDecimal price = quote.getPrice();
            BigDecimal orderFee = order.getOrderFee();
            BigDecimal balance = account.getBalance();
            total = (new BigDecimal(quantity).multiply(price)).subtract(orderFee);
            account.setBalance(balance.add(total));

            if (orderProcessingMode == TradeConfig.SYNCH) {
                completeOrder(order.getOrderID(), false);
            } else if (orderProcessingMode == TradeConfig.ASYNCH_2PHASE) {
                queueOrder(order.getOrderID(), true);
            }
        } catch (Exception e) {
            Log.error("TradeServiceImpl:sell(" + userID + "," + holdingID + ") --> failed", e);
            throw new TradeException("TradeServiceImpl:sell(" + userID + "," + holdingID + ")", e);
        }
        return order;
    }

    @Override
    public void queueOrder(Integer orderID, boolean twoPhase) {
        throw new UnsupportedOperationException(
                "TradeServiceImpl:queueOrder -- JMS/async order processing is deferred from the trade-services slice; "
                        + "use orderProcessingMode=TradeConfig.SYNCH");
    }

    @Override
    public OrderDataBean completeOrder(Integer orderID, boolean twoPhase) throws Exception {
        if (Log.doTrace()) {
            Log.trace("TradeServiceImpl:completeOrder", orderID + " twoPhase=" + twoPhase);
        }

        OrderDataBean order = orderRepository.findById(orderID).orElse(null);

        if (order == null) {
            Log.error("TradeServiceImpl:completeOrder -- Unable to find Order " + orderID + " FBPK returned " + order);
            return null;
        }

        if (order.isCompleted()) {
            throw new TradeException("Error: attempt to complete Order that is already completed\n" + order);
        }

        AccountDataBean account = order.getAccount();
        QuoteDataBean quote = order.getQuote();
        HoldingDataBean holding = order.getHolding();
        BigDecimal price = order.getPrice();
        double quantity = order.getQuantity();

        if (Log.doTrace()) {
            Log.trace("TradeServiceImpl:completeOrder--> Completing Order " + order.getOrderID()
                    + "\n\t Order info: " + order
                    + "\n\t Account info: " + account
                    + "\n\t Quote info: " + quote
                    + "\n\t Holding info: " + holding);
        }

        if (order.isBuy()) {
            /*
             * Complete a Buy operation - create a new Holding for the Account -
             * deduct the Order cost from the Account balance
             */
            HoldingDataBean newHolding = createHolding(account, quote, quantity, price);
            order.setHolding(newHolding);
        }

        if (order.isSell()) {
            /*
             * Complete a Sell operation - remove the Holding from the Account -
             * deposit the Order proceeds to the Account balance
             */
            if (holding == null) {
                Log.error("TradeServiceImpl:completeOrder -- Unable to sell order " + order.getOrderID()
                        + " holding already sold");
                order.cancel();
                return order;
            } else {
                holdingRepository.delete(holding);
                order.setHolding(null);
            }
        }

        order.setOrderStatus("closed");
        order.setCompletionDate(new java.sql.Timestamp(System.currentTimeMillis()));

        if (Log.doTrace()) {
            Log.trace("TradeServiceImpl:completeOrder--> Completed Order " + order.getOrderID()
                    + "\n\t Order info: " + order
                    + "\n\t Account info: " + account
                    + "\n\t Quote info: " + quote
                    + "\n\t Holding info: " + holding);
        }

        orderRepository.save(order);

        return order;
    }

    @Override
    public void cancelOrder(Integer orderID, boolean twoPhase) {
        if (Log.doTrace()) {
            Log.trace("TradeServiceImpl:cancelOrder", orderID + " twoPhase=" + twoPhase);
        }

        OrderDataBean order = orderRepository.findById(orderID).orElse(null);
        if (order != null) {
            order.cancel();
            orderRepository.save(order);
        }
    }

    @Override
    public void orderCompleted(String userID, Integer orderID) {
        throw new UnsupportedOperationException("TradeServiceImpl:orderCompleted method not supported");
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<OrderDataBean> getOrders(String userID) {
        if (Log.doTrace()) {
            Log.trace("TradeServiceImpl:getOrders", userID);
        }
        return orderRepository.findByUserID(userID);
    }

    @Override
    public Collection<OrderDataBean> getClosedOrders(String userID) {
        if (Log.doTrace()) {
            Log.trace("TradeServiceImpl:getClosedOrders", userID);
        }

        try {
            List<OrderDataBean> results = orderRepository.findClosedOrders(userID);

            // Spin through the orders to populate the lazy quote fields and mark as complete
            for (OrderDataBean thisOrder : results) {
                thisOrder.getQuote();
                thisOrder.setOrderStatus("completed");
            }

            return results;
        } catch (Exception e) {
            Log.error("TradeServiceImpl.getClosedOrders", e);
            throw new TradeException("TradeServiceImpl.getClosedOrders - error", e);
        }
    }

    @Override
    public QuoteDataBean createQuote(String symbol, String companyName, BigDecimal price) {
        try {
            QuoteDataBean quote = new QuoteDataBean(symbol, companyName, 0, price, price, price, price, 0);
            quoteRepository.save(quote);
            if (Log.doTrace()) {
                Log.trace("TradeServiceImpl:createQuote-->" + quote);
            }
            return quote;
        } catch (Exception e) {
            Log.error("TradeServiceImpl:createQuote -- exception creating Quote", e);
            throw new TradeException(e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public QuoteDataBean getQuote(String symbol) {
        if (Log.doTrace()) {
            Log.trace("TradeServiceImpl:getQuote", symbol);
        }
        return quoteRepository.findById(symbol).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<QuoteDataBean> getAllQuotes() {
        if (Log.doTrace()) {
            Log.trace("TradeServiceImpl:getAllQuotes");
        }
        return quoteRepository.findAll();
    }

    @Override
    public QuoteDataBean updateQuotePriceVolume(String symbol, BigDecimal changeFactor, double sharesTraded) {
        if (!TradeConfig.getUpdateQuotePrices()) {
            return new QuoteDataBean();
        }

        if (Log.doTrace()) {
            Log.trace("TradeServiceImpl:updateQuote", symbol, changeFactor);
        }

        QuoteDataBean quote = quoteRepository.findBySymbolForUpdate(symbol)
                .orElseThrow(() -> new TradeException("TradeServiceImpl:updateQuotePriceVolume -- no such quote " + symbol));

        BigDecimal oldPrice = quote.getPrice();

        if (quote.getPrice().equals(TradeConfig.PENNY_STOCK_PRICE)) {
            changeFactor = TradeConfig.PENNY_STOCK_RECOVERY_MIRACLE_MULTIPLIER;
        }

        BigDecimal newPrice = changeFactor.multiply(oldPrice).setScale(2, BigDecimal.ROUND_HALF_UP);

        quote.setPrice(newPrice);
        quote.setVolume(quote.getVolume() + sharesTraded);
        quoteRepository.save(quote);

        publishQuotePriceChange(quote, oldPrice, changeFactor, sharesTraded);

        return quote;
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<HoldingDataBean> getHoldings(String userID) {
        if (Log.doTrace()) {
            Log.trace("TradeServiceImpl:getHoldings", userID);
        }

        List<HoldingDataBean> holdings = holdingRepository.findByUserID(userID);
        // Inflate the lazy data members
        for (HoldingDataBean holding : holdings) {
            holding.getQuote();
        }
        return holdings;
    }

    @Override
    @Transactional(readOnly = true)
    public HoldingDataBean getHolding(Integer holdingID) {
        if (Log.doTrace()) {
            Log.trace("TradeServiceImpl:getHolding", holdingID);
        }
        return holdingRepository.findById(holdingID).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDataBean getAccountData(String userID) {
        if (Log.doTrace()) {
            Log.trace("TradeServiceImpl:getAccountData", userID);
        }

        AccountProfileDataBean profile = accountProfileRepository.findById(userID).orElse(null);
        if (profile == null) {
            return null;
        }
        AccountDataBean account = profile.getAccount();
        account.getProfile();
        // populate transient field for account
        account.setProfileID(profile.getUserID());
        return account;
    }

    @Override
    @Transactional(readOnly = true)
    public AccountProfileDataBean getAccountProfileData(String userID) {
        if (Log.doTrace()) {
            Log.trace("TradeServiceImpl:getProfileData", userID);
        }
        return accountProfileRepository.findById(userID).orElse(null);
    }

    @Override
    public AccountProfileDataBean updateAccountProfile(AccountProfileDataBean profileData) {
        if (Log.doTrace()) {
            Log.trace("TradeServiceImpl:updateAccountProfileData", profileData);
        }

        AccountProfileDataBean temp = accountProfileRepository.findById(profileData.getUserID()).orElse(null);
        if (temp == null) {
            return null;
        }
        temp.setAddress(profileData.getAddress());
        temp.setPassword(profileData.getPassword());
        temp.setFullName(profileData.getFullName());
        temp.setCreditCard(profileData.getCreditCard());
        temp.setEmail(profileData.getEmail());

        accountProfileRepository.save(temp);

        return temp;
    }

    @Override
    public AccountDataBean login(String userID, String password) {
        AccountProfileDataBean profile = accountProfileRepository.findById(userID).orElse(null);

        if (profile == null) {
            throw new TradeException("No such user: " + userID);
        }
        AccountDataBean account = profile.getAccount();

        if (Log.doTrace()) {
            Log.trace("TradeServiceImpl:login", userID, password);
        }
        account.login(password);
        if (Log.doTrace()) {
            Log.trace("TradeServiceImpl:login(" + userID + "," + password + ") success" + account);
        }
        return account;
    }

    @Override
    public void logout(String userID) {
        if (Log.doTrace()) {
            Log.trace("TradeServiceImpl:logout", userID);
        }

        AccountProfileDataBean profile = accountProfileRepository.findById(userID).orElse(null);
        AccountDataBean account = profile.getAccount();
        account.logout();

        if (Log.doTrace()) {
            Log.trace("TradeServiceImpl:logout(" + userID + ") success");
        }
    }

    @Override
    public AccountDataBean register(String userID, String password, String fullname, String address, String email,
            String creditcard, BigDecimal openBalance) {
        AccountDataBean account = null;
        AccountProfileDataBean profile = null;

        if (Log.doTrace()) {
            Log.trace("TradeServiceImpl:register", userID, password, fullname, address, email, creditcard, openBalance);
        }

        // Check to see if a profile with the desired userID already exists
        profile = accountProfileRepository.findById(userID).orElse(null);

        if (profile != null) {
            Log.error("Failed to register new Account - AccountProfile with userID(" + userID + ") already exists");
            return null;
        } else {
            profile = new AccountProfileDataBean(userID, password, fullname, address, email, creditcard);
            account = new AccountDataBean(0, 0, null, new Timestamp(System.currentTimeMillis()), openBalance,
                    openBalance, userID);

            profile.setAccount(account);
            account.setProfile(profile);

            // CascadeType.ALL on profile.account persists the account (FK target
            // profile is inserted first), matching the legacy persist ordering.
            accountProfileRepository.save(profile);
        }

        return account;
    }

    @Override
    public RunStatsDataBean resetTrade(boolean deleteAll) {
        throw new UnsupportedOperationException(
                "TradeServiceImpl:resetTrade -- the bulk direct-JDBC reset path is deferred from the trade-services slice");
    }

    /**
     * JMS publishing of quote price changes is deferred from this slice. Retained
     * as a no-op honoring the {@code publishQuotePriceChange} feature flag so the
     * pricing path keeps the same call shape as the legacy bean.
     */
    private void publishQuotePriceChange(QuoteDataBean quote, BigDecimal oldPrice, BigDecimal changeFactor,
            double sharesTraded) {
        if (!TradeConfig.getPublishQuotePriceChange()) {
            return;
        }
        throw new UnsupportedOperationException(
                "TradeServiceImpl:publishQuotePriceChange -- JMS topic publishing is deferred from the trade-services slice");
    }

    private OrderDataBean createOrder(AccountDataBean account, QuoteDataBean quote, HoldingDataBean holding,
            String orderType, double quantity) {
        OrderDataBean order;

        if (Log.doTrace()) {
            Log.trace("TradeServiceImpl:createOrder(orderID="
                    + " account=" + ((account == null) ? null : account.getAccountID())
                    + " quote=" + ((quote == null) ? null : quote.getSymbol())
                    + " orderType=" + orderType
                    + " quantity=" + quantity);
        }
        try {
            order = new OrderDataBean(orderType, "open", new Timestamp(System.currentTimeMillis()), null, quantity,
                    quote.getPrice().setScale(FinancialUtils.SCALE, FinancialUtils.ROUND),
                    TradeConfig.getOrderFee(orderType), account, quote, holding);
            orderRepository.save(order);
        } catch (Exception e) {
            Log.error("TradeServiceImpl:createOrder -- failed to create Order", e);
            throw new TradeException("TradeServiceImpl:createOrder -- failed to create Order", e);
        }
        return order;
    }

    private HoldingDataBean createHolding(AccountDataBean account, QuoteDataBean quote, double quantity,
            BigDecimal purchasePrice) {
        HoldingDataBean newHolding = new HoldingDataBean(quantity, purchasePrice,
                new Timestamp(System.currentTimeMillis()), account, quote);
        holdingRepository.save(newHolding);
        return newHolding;
    }
}
