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

import java.math.BigDecimal;
import java.util.Date;

import org.junit.jupiter.api.Test;

/**
 * Regression tests for holdings: quantity, purchase price and the symbol a
 * holding is associated with.
 */
public class HoldingDataBeanTest {

    @Test
    public void holdingRetainsQuantityAndPurchasePrice() {
        HoldingDataBean holding = new HoldingDataBean(10.0d,
                new BigDecimal("50.00"), new Date(), null, null);
        assertEquals(10.0d, holding.getQuantity());
        assertEquals(new BigDecimal("50.00"), holding.getPurchasePrice());
    }

    @Test
    public void quoteIdResolvesFromAssociatedQuote() {
        HoldingDataBean holding = new HoldingDataBean(5.0d, new BigDecimal("12.00"), new Date(), null, null);
        holding.setQuoteID("s:7");
        assertEquals("s:7", holding.getQuoteID());

        holding.setQuote(new QuoteDataBean("s:8"));
        assertEquals("s:8", holding.getQuoteID());
    }
}
