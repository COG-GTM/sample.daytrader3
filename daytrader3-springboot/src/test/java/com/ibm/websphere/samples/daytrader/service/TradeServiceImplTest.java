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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.websphere.samples.daytrader.TradeServices;
import com.ibm.websphere.samples.daytrader.domain.AccountDataBean;
import com.ibm.websphere.samples.daytrader.domain.HoldingDataBean;
import com.ibm.websphere.samples.daytrader.domain.OrderDataBean;
import com.ibm.websphere.samples.daytrader.domain.QuoteDataBean;
import com.ibm.websphere.samples.daytrader.dto.MarketSummaryDataBean;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;
import com.ibm.websphere.samples.daytrader.util.TradeException;

/**
 * Integration tests for the migrated {@link TradeServiceImpl}. Each test runs in a
 * transaction that is rolled back afterwards, against an in-memory H2 database, and
 * exercises the synchronous trade-services behavior ported from the legacy EJB.
 */
@SpringBootTest
@Transactional
class TradeServiceImplTest {

    @Autowired
    private TradeServices trade;

    private static final String USER = "uid:test";
    private static final String SYMBOL = "s:100";

    @BeforeEach
    void seed() throws Exception {
        trade.register(USER, "password", "Test User", "123 Test St", "test@example.com", "1234-5678", new BigDecimal("100000.00"));
        trade.createQuote(SYMBOL, "Test Company", new BigDecimal("25.00"));
    }

    @Test
    void registerCreatesAccountWithOpenBalance() throws Exception {
        AccountDataBean account = trade.getAccountData(USER);
        assertNotNull(account);
        assertEquals(0, new BigDecimal("100000.00").compareTo(account.getBalance()));
        assertEquals(USER, account.getProfileID());
    }

    @Test
    void registerDuplicateUserReturnsNull() throws Exception {
        AccountDataBean dup = trade.register(USER, "x", "x", "x", "x@x.com", "x", new BigDecimal("1.00"));
        assertNull(dup);
    }

    @Test
    void loginIncrementsLoginCountAndLogoutWorks() throws Exception {
        AccountDataBean account = trade.login(USER, "password");
        assertNotNull(account);
        assertTrue(account.getLoginCount() >= 1);

        int loginCount = account.getLoginCount();
        trade.logout(USER);
        AccountDataBean after = trade.getAccountData(USER);
        assertEquals(loginCount, after.getLoginCount());
        assertTrue(after.getLogoutCount() >= 1);
    }

    @Test
    void loginWithBadPasswordThrows() {
        assertThrows(TradeException.class, () -> trade.login(USER, "wrong"));
    }

    @Test
    void getQuoteAndAllQuotes() throws Exception {
        QuoteDataBean quote = trade.getQuote(SYMBOL);
        assertNotNull(quote);
        assertEquals("Test Company", quote.getCompanyName());
        assertEquals(0, new BigDecimal("25.00").compareTo(quote.getPrice()));

        Collection<?> all = trade.getAllQuotes();
        assertFalse(all.isEmpty());
    }

    @Test
    void buyCreatesHoldingAndDebitsBalance() throws Exception {
        BigDecimal startBalance = trade.getAccountData(USER).getBalance();

        OrderDataBean order = trade.buy(USER, SYMBOL, 10, TradeConfig.SYNCH);
        assertNotNull(order);
        assertTrue(order.isBuy());
        assertEquals("closed", order.getOrderStatus());

        Collection<?> holdings = trade.getHoldings(USER);
        assertEquals(1, holdings.size());

        // 10 shares * $25 + order fee debited from balance
        BigDecimal cost = new BigDecimal("250.00").add(TradeConfig.getOrderFee("buy"));
        BigDecimal expected = startBalance.subtract(cost);
        assertEquals(0, expected.compareTo(trade.getAccountData(USER).getBalance()));
    }

    @Test
    void sellRemovesHoldingAndCreditsBalance() throws Exception {
        trade.buy(USER, SYMBOL, 10, TradeConfig.SYNCH);
        Collection<?> holdings = trade.getHoldings(USER);
        assertEquals(1, holdings.size());
        Integer holdingID = ((HoldingDataBean) holdings.iterator().next()).getHoldingID();

        BigDecimal balanceBeforeSell = trade.getAccountData(USER).getBalance();

        OrderDataBean sellOrder = trade.sell(USER, holdingID, TradeConfig.SYNCH);
        assertNotNull(sellOrder);
        assertTrue(sellOrder.isSell());
        assertEquals("closed", sellOrder.getOrderStatus());

        assertTrue(trade.getHoldings(USER).isEmpty());

        // proceeds: 10 * $25 - order fee credited
        BigDecimal proceeds = new BigDecimal("250.00").subtract(TradeConfig.getOrderFee("sell"));
        BigDecimal expected = balanceBeforeSell.add(proceeds);
        assertEquals(0, expected.compareTo(trade.getAccountData(USER).getBalance()));
    }

    @Test
    void getOrdersReturnsPlacedOrders() throws Exception {
        trade.buy(USER, SYMBOL, 5, TradeConfig.SYNCH);
        Collection<?> orders = trade.getOrders(USER);
        assertEquals(1, orders.size());
    }

    @Test
    void getMarketSummaryComputesTsiaAndTopMovers() throws Exception {
        // Need > 5 quotes matching the 's:1__' index pattern
        for (int i = 1; i <= 9; i++) {
            trade.createQuote("s:10" + i, "Company " + i, new BigDecimal("10.00").add(new BigDecimal(i)));
        }

        MarketSummaryDataBean summary = trade.getMarketSummary();
        assertNotNull(summary);
        assertNotNull(summary.getTSIA());
        assertEquals(5, summary.getTopGainers().size());
        assertEquals(5, summary.getTopLosers().size());
    }

    @Test
    void updateAccountProfilePersistsChanges() throws Exception {
        var profile = trade.getAccountProfileData(USER);
        profile.setFullName("Updated Name");
        profile.setAddress("456 New Ave");
        var updated = trade.updateAccountProfile(profile);
        assertEquals("Updated Name", updated.getFullName());
        assertEquals("456 New Ave", trade.getAccountProfileData(USER).getAddress());
    }

    @Test
    void queueOrderAndResetTradeAreDeferred() {
        assertThrows(UnsupportedOperationException.class, () -> trade.queueOrder(1, true));
        assertThrows(UnsupportedOperationException.class, () -> trade.resetTrade(true));
    }
}
