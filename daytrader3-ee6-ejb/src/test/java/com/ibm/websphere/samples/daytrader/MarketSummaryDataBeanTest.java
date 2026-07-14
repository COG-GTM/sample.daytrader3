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
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

/**
 * Regression test for the market summary gain percentage shown on the home page.
 */
public class MarketSummaryDataBeanTest {

    @Test
    public void gainPercentIsComputedFromTsiaAndOpenTsia() {
        MarketSummaryDataBean summary = new MarketSummaryDataBean(
                new BigDecimal("110.00"), new BigDecimal("100.00"), 1234d,
                new ArrayList<QuoteDataBean>(), new ArrayList<QuoteDataBean>());

        assertEquals(0, new BigDecimal("10").compareTo(summary.getGainPercent()));
    }
}
