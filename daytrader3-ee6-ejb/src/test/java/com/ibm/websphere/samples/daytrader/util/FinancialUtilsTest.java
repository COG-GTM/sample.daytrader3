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
package com.ibm.websphere.samples.daytrader.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.junit.jupiter.api.Test;

import com.ibm.websphere.samples.daytrader.HoldingDataBean;

/**
 * Regression tests for the portfolio P&amp;L math that drives user-visible gains
 * and holdings valuation.
 */
public class FinancialUtilsTest {

    @Test
    public void computeGainIsCurrentMinusOpen() {
        BigDecimal gain = FinancialUtils.computeGain(new BigDecimal("110.00"), new BigDecimal("100.00"));
        assertEquals(0, new BigDecimal("10.00").compareTo(gain));
    }

    @Test
    public void computeGainPercentIsRelativeChangeTimesHundred() {
        BigDecimal pct = FinancialUtils.computeGainPercent(new BigDecimal("110.00"), new BigDecimal("100.00"));
        assertEquals(0, new BigDecimal("10").compareTo(pct));
    }

    @Test
    public void computeGainPercentGuardsAgainstZeroOpenBalance() {
        BigDecimal pct = FinancialUtils.computeGainPercent(new BigDecimal("110.00"), new BigDecimal("0.00"));
        assertEquals(0, FinancialUtils.ZERO.compareTo(pct));
    }

    @Test
    public void computeHoldingsTotalSumsPriceTimesQuantity() {
        Collection<HoldingDataBean> holdings = new ArrayList<>();
        holdings.add(new HoldingDataBean(10.0d, new BigDecimal("50.00"), new Date(), null, null));
        holdings.add(new HoldingDataBean(2.0d, new BigDecimal("25.00"), new Date(), null, null));

        BigDecimal total = FinancialUtils.computeHoldingsTotal(holdings);
        // (10 * 50.00) + (2 * 25.00) = 550.00
        assertEquals(0, new BigDecimal("550.00").compareTo(total));
    }

    @Test
    public void computeHoldingsTotalOfNullIsZero() {
        assertEquals(0, FinancialUtils.ZERO.compareTo(FinancialUtils.computeHoldingsTotal(null)));
    }
}
