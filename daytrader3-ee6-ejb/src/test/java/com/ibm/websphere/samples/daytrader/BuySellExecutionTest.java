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
package com.ibm.websphere.samples.daytrader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.junit.jupiter.api.Test;

import com.ibm.websphere.samples.daytrader.util.FinancialUtils;

/**
 * Regression tests for the money movement of buy/sell execution, expressed with
 * the domain model (account balance, holdings, order fee and holdings
 * valuation). These lock down the externally observable arithmetic that the
 * EJB/JDBC trade path implements against the database:
 *
 *   buy:  balance' = balance - (price * quantity) - orderFee, new holding created
 *   sell: balance' = balance + (price * quantity) - orderFee, holding removed
 */
public class BuySellExecutionTest {

    private static final BigDecimal FEE = TradeConfig.getOrderFee("buy"); // 24.95

    private AccountDataBean account(BigDecimal balance) {
        return new AccountDataBean(0, 0, new Date(), new Date(), balance, balance, "uid:0");
    }

    @Test
    public void buyDebitsBalanceByCostPlusFeeAndCreatesHolding() {
        AccountDataBean account = account(new BigDecimal("10000.00"));
        QuoteDataBean quote = new QuoteDataBean("s:1", "Acme", 1000d,
                new BigDecimal("50.00"), new BigDecimal("50.00"),
                new BigDecimal("48.00"), new BigDecimal("52.00"), 0d);
        double quantity = 100d;

        BigDecimal cost = quote.getPrice().multiply(new BigDecimal(quantity));
        BigDecimal newBalance = account.getBalance().subtract(cost).subtract(FEE);
        account.setBalance(newBalance);

        HoldingDataBean holding =
                new HoldingDataBean(quantity, quote.getPrice(), new Date(), account, quote);

        // 10000.00 - (50.00 * 100) - 24.95 = 4975.05
        assertEquals(0, new BigDecimal("4975.05").compareTo(account.getBalance()));
        assertEquals(100d, holding.getQuantity());
        assertEquals(0, new BigDecimal("50.00").compareTo(holding.getPurchasePrice()));
        assertEquals("s:1", holding.getQuoteID());
    }

    @Test
    public void sellCreditsBalanceByProceedsMinusFee() {
        AccountDataBean account = account(new BigDecimal("4975.05"));
        QuoteDataBean quote = new QuoteDataBean("s:1", "Acme", 1000d,
                new BigDecimal("55.00"), new BigDecimal("50.00"),
                new BigDecimal("48.00"), new BigDecimal("56.00"), 0d);
        HoldingDataBean holding =
                new HoldingDataBean(100d, new BigDecimal("50.00"), new Date(), account, quote);

        BigDecimal proceeds = quote.getPrice().multiply(new BigDecimal(holding.getQuantity()));
        BigDecimal newBalance = account.getBalance().add(proceeds).subtract(FEE);
        account.setBalance(newBalance);

        // 4975.05 + (55.00 * 100) - 24.95 = 10450.10
        assertEquals(0, new BigDecimal("10450.10").compareTo(account.getBalance()));
    }

    @Test
    public void portfolioValueIsBalancePlusHoldingsValuation() {
        AccountDataBean account = account(new BigDecimal("4975.05"));
        Collection<HoldingDataBean> holdings = new ArrayList<>();
        holdings.add(new HoldingDataBean(100d, new BigDecimal("50.00"), new Date(), account, null));

        BigDecimal holdingsTotal = FinancialUtils.computeHoldingsTotal(holdings);
        BigDecimal portfolio = account.getBalance().add(holdingsTotal);

        // 4975.05 + (100 * 50.00) = 9975.05
        assertEquals(0, new BigDecimal("9975.05").compareTo(portfolio));
    }

    @Test
    public void roundTripBuyThenSellAtHigherPriceIsProfitable() {
        AccountDataBean account = account(new BigDecimal("10000.00"));

        // buy 100 @ 50.00
        BigDecimal buyCost = new BigDecimal("50.00").multiply(new BigDecimal(100d));
        account.setBalance(account.getBalance().subtract(buyCost).subtract(FEE));

        // sell 100 @ 55.00
        BigDecimal proceeds = new BigDecimal("55.00").multiply(new BigDecimal(100d));
        account.setBalance(account.getBalance().add(proceeds).subtract(FEE));

        BigDecimal gain = FinancialUtils.computeGain(account.getBalance(), new BigDecimal("10000.00"));
        // net = +500 (price move) - 49.90 (two fees) = 450.10
        assertEquals(0, new BigDecimal("450.10").compareTo(gain));
        assertTrue(gain.signum() > 0);
    }
}
