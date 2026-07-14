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

import org.junit.jupiter.api.Test;

/**
 * Regression tests for quote retrieval: the values a user sees when looking up
 * a stock quote (symbol, company, price, open/high/low, change, volume).
 */
public class QuoteDataBeanTest {

    @Test
    public void quoteRetainsAllRetrievedFields() {
        QuoteDataBean quote = new QuoteDataBean("s:1", "s:1 Incorporated",
                7500.0d, new BigDecimal("55.25"), new BigDecimal("50.00"),
                new BigDecimal("49.10"), new BigDecimal("60.00"), 5.25d);

        assertEquals("s:1", quote.getSymbol());
        assertEquals("s:1 Incorporated", quote.getCompanyName());
        assertEquals(7500.0d, quote.getVolume());
        assertEquals(new BigDecimal("55.25"), quote.getPrice());
        assertEquals(new BigDecimal("50.00"), quote.getOpen());
        assertEquals(new BigDecimal("49.10"), quote.getLow());
        assertEquals(new BigDecimal("60.00"), quote.getHigh());
        assertEquals(5.25d, quote.getChange());
    }

    @Test
    public void zeroValueQuoteExposesOnlySymbol() {
        QuoteDataBean quote = new QuoteDataBean("s:42");
        assertEquals("s:42", quote.getSymbol());
    }

    @Test
    public void priceChangeIsMutable() {
        QuoteDataBean quote = new QuoteDataBean("s:2");
        quote.setPrice(new BigDecimal("10.00"));
        quote.setChange(-1.5d);
        assertEquals(new BigDecimal("10.00"), quote.getPrice());
        assertEquals(-1.5d, quote.getChange());
    }
}
