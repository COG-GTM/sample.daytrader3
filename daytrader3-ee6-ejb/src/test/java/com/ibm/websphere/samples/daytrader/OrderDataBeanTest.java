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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.jupiter.api.Test;

/**
 * Regression tests around order creation and order lifecycle classification.
 * These lock down the externally observable buy/sell order semantics.
 */
public class OrderDataBeanTest {

    private OrderDataBean newOrder(String type, String status) {
        return new OrderDataBean(type, status, new Date(), null,
                100.0d, new BigDecimal("12.34"),
                TradeConfig.getOrderFee(type), null, null, null);
    }

    @Test
    public void buyOrderIsClassifiedAsBuyAndOpen() {
        OrderDataBean order = newOrder("buy", "open");
        assertTrue(order.isBuy());
        assertFalse(order.isSell());
        assertTrue(order.isOpen());
        assertFalse(order.isCompleted());
        assertEquals(new BigDecimal("24.95"), order.getOrderFee());
    }

    @Test
    public void sellOrderIsClassifiedAsSell() {
        OrderDataBean order = newOrder("sell", "open");
        assertTrue(order.isSell());
        assertFalse(order.isBuy());
        assertEquals(new BigDecimal("24.95"), order.getOrderFee());
    }

    @Test
    public void processingOrderIsStillOpen() {
        OrderDataBean order = newOrder("buy", "processing");
        assertTrue(order.isOpen());
    }

    @Test
    public void completedOrderIsNotOpen() {
        OrderDataBean order = newOrder("buy", "completed");
        assertTrue(order.isCompleted());
        assertFalse(order.isOpen());
    }

    @Test
    public void cancelMovesOrderToCancelledState() {
        OrderDataBean order = newOrder("buy", "open");
        order.cancel();
        assertEquals("cancelled", order.getOrderStatus());
        assertTrue(order.isCancelled());
        assertTrue(order.isCompleted());
    }

    @Test
    public void symbolResolvesFromQuoteWhenPresent() {
        OrderDataBean order = newOrder("buy", "open");
        order.setSymbol("s:1");
        assertEquals("s:1", order.getSymbol());

        QuoteDataBean quote = new QuoteDataBean("s:99");
        order.setQuote(quote);
        assertEquals("s:99", order.getSymbol());
    }
}
