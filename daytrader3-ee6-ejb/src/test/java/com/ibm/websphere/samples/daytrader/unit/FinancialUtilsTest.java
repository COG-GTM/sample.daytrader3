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

import org.junit.Test;

import com.ibm.websphere.samples.daytrader.HoldingDataBean;
import com.ibm.websphere.samples.daytrader.util.FinancialUtils;

public class FinancialUtilsTest {

    private static HoldingDataBean holding(String purchasePrice, double quantity) {
        HoldingDataBean holding = new HoldingDataBean();
        holding.setPurchasePrice(new BigDecimal(purchasePrice));
        holding.setQuantity(quantity);
        return holding;
    }

    @Test
    public void computeGain_positiveGain() {
        BigDecimal gain = FinancialUtils.computeGain(new BigDecimal("110.00"), new BigDecimal("100.00"));

        assertEquals(0, gain.compareTo(new BigDecimal("10.00")));
        assertEquals(FinancialUtils.SCALE, gain.scale());
    }

    @Test
    public void computeGain_negativeGain() {
        BigDecimal gain = FinancialUtils.computeGain(new BigDecimal("90.50"), new BigDecimal("100.00"));

        assertEquals(0, gain.compareTo(new BigDecimal("-9.50")));
        assertEquals(FinancialUtils.SCALE, gain.scale());
    }

    @Test
    public void computeGain_zeroDifference() {
        BigDecimal gain = FinancialUtils.computeGain(new BigDecimal("100.00"), new BigDecimal("100.00"));

        assertEquals(0, gain.compareTo(FinancialUtils.ZERO));
        assertEquals(FinancialUtils.SCALE, gain.scale());
    }

    @Test
    public void computeGainPercent_normalCalculation() {
        BigDecimal gainPercent =
                FinancialUtils.computeGainPercent(new BigDecimal("110.00"), new BigDecimal("100.00"));

        assertEquals(0, gainPercent.compareTo(new BigDecimal("10")));
    }

    @Test
    public void computeGainPercent_loss() {
        BigDecimal gainPercent =
                FinancialUtils.computeGainPercent(new BigDecimal("75.00"), new BigDecimal("100.00"));

        assertEquals(0, gainPercent.compareTo(new BigDecimal("-25")));
    }

    @Test
    public void computeGainPercent_zeroOpenBalanceReturnsZero() {
        BigDecimal gainPercent =
                FinancialUtils.computeGainPercent(new BigDecimal("110.00"), new BigDecimal("0.00"));

        assertEquals(FinancialUtils.ZERO, gainPercent);
    }

    @Test
    public void computeHoldingsTotal_multipleHoldings() {
        Collection<HoldingDataBean> holdings = new ArrayList<HoldingDataBean>();
        holdings.add(holding("10.00", 100.0));
        holdings.add(holding("20.50", 3.0));

        BigDecimal total = FinancialUtils.computeHoldingsTotal(holdings);

        assertEquals(0, total.compareTo(new BigDecimal("1061.50")));
        assertEquals(FinancialUtils.SCALE, total.scale());
    }

    @Test
    public void computeHoldingsTotal_emptyCollectionReturnsZero() {
        BigDecimal total = FinancialUtils.computeHoldingsTotal(new ArrayList<HoldingDataBean>());

        assertEquals(0, total.compareTo(FinancialUtils.ZERO));
        assertEquals(FinancialUtils.SCALE, total.scale());
    }

    @Test
    public void computeHoldingsTotal_nullCollectionReturnsZero() {
        BigDecimal total = FinancialUtils.computeHoldingsTotal(null);

        assertEquals(0, total.compareTo(FinancialUtils.ZERO));
        assertEquals(FinancialUtils.SCALE, total.scale());
    }
}
