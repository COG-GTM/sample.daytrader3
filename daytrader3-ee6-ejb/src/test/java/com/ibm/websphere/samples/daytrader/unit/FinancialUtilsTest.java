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
package com.ibm.websphere.samples.daytrader.unit;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

import com.ibm.websphere.samples.daytrader.HoldingDataBean;
import com.ibm.websphere.samples.daytrader.util.FinancialUtils;

public class FinancialUtilsTest {

    private static BigDecimal money(String value) {
        return new BigDecimal(value).setScale(FinancialUtils.SCALE);
    }

    private static HoldingDataBean holding(double quantity, String purchasePrice) {
        HoldingDataBean holding = new HoldingDataBean();
        holding.setQuantity(quantity);
        holding.setPurchasePrice(money(purchasePrice));
        return holding;
    }

    @Test
    public void computeGain_positiveGain_returnsDifferenceWithScale() {
        BigDecimal gain = FinancialUtils.computeGain(money("150.75"), money("100.25"));

        assertEquals(money("50.50"), gain);
        assertEquals(FinancialUtils.SCALE, gain.scale());
    }

    @Test
    public void computeGain_loss_returnsNegativeDifference() {
        BigDecimal gain = FinancialUtils.computeGain(money("80.00"), money("100.25"));

        assertEquals(money("-20.25"), gain);
        assertEquals(FinancialUtils.SCALE, gain.scale());
    }

    @Test
    public void computeGain_equalBalances_returnsZero() {
        BigDecimal gain = FinancialUtils.computeGain(money("100.25"), money("100.25"));

        assertEquals(FinancialUtils.ZERO, gain);
        assertEquals(FinancialUtils.SCALE, gain.scale());
    }

    @Test
    public void computeGainPercent_normalCalculation_returnsPercentGain() {
        BigDecimal gainPercent = FinancialUtils.computeGainPercent(money("110.00"), money("100.00"));

        assertEquals(0, gainPercent.compareTo(new BigDecimal("10")));
    }

    @Test
    public void computeGainPercent_loss_returnsNegativePercent() {
        BigDecimal gainPercent = FinancialUtils.computeGainPercent(money("90.00"), money("100.00"));

        assertEquals(0, gainPercent.compareTo(new BigDecimal("-10")));
    }

    @Test
    public void computeGainPercent_zeroOpenBalance_returnsZero() {
        assertEquals(FinancialUtils.ZERO, FinancialUtils.computeGainPercent(money("110.00"), money("0.00")));
    }

    @Test
    public void computeHoldingsTotal_collectionOfHoldings_returnsSumOfPriceTimesQuantity() {
        Collection<HoldingDataBean> holdings = new ArrayList<HoldingDataBean>();
        holdings.add(holding(10.0, "25.50"));
        holdings.add(holding(3.0, "100.00"));

        BigDecimal total = FinancialUtils.computeHoldingsTotal(holdings);

        assertEquals(money("555.00"), total);
        assertEquals(FinancialUtils.SCALE, total.scale());
    }

    @Test
    public void computeHoldingsTotal_emptyCollection_returnsZero() {
        BigDecimal total = FinancialUtils.computeHoldingsTotal(Collections.emptyList());

        assertEquals(FinancialUtils.ZERO, total);
        assertEquals(FinancialUtils.SCALE, total.scale());
    }

    @Test
    public void computeHoldingsTotal_nullCollection_returnsZero() {
        BigDecimal total = FinancialUtils.computeHoldingsTotal(null);

        assertEquals(FinancialUtils.ZERO, total);
        assertEquals(FinancialUtils.SCALE, total.scale());
    }
}
