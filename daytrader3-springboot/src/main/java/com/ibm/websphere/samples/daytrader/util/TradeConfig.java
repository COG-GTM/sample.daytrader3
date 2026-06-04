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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Random;

/**
 * Holds the configuration and runtime parameters for the Trade application that
 * are still relevant to the migrated trade-services slice. Container-, JSP- and
 * benchmark-driver-specific parameters from the legacy {@code TradeConfig} are
 * intentionally omitted; the order-processing constants, fee schedule, quote
 * pricing flags and random-data helpers are preserved with identical behavior.
 */
public final class TradeConfig {

    private TradeConfig() {
    }

    /* Trade Runtime Mode parameters (kept for contract compatibility) */
    public static final int EJB3 = 0;
    public static final int DIRECT = 1;
    public static final int SESSION3 = 2;

    /* Order processing modes */
    public static final int SYNCH = 0;
    public static final int ASYNCH_2PHASE = 1;
    public static int orderProcessingMode = SYNCH;

    /* Trade Database Scaling parameters */
    private static int MAX_USERS = 15000;
    private static int MAX_QUOTES = 10000;
    public static int QUOTES_PER_PAGE = 10;

    private static final Random randomNumberGenerator = new Random(System.currentTimeMillis());

    public static final String newUserPrefix = "ru:";

    private static boolean updateQuotePrices = true;
    private static boolean publishQuotePriceChange = true;
    private static int marketSummaryInterval = 20;

    /*
     * Penny stocks problem: the random price change factor can drive a stock down
     * to $.01. In that case Trade jumpstarts the price back up to keep the math
     * interesting.
     */
    public static final BigDecimal PENNY_STOCK_PRICE;
    public static final BigDecimal PENNY_STOCK_RECOVERY_MIRACLE_MULTIPLIER;
    static {
        PENNY_STOCK_PRICE = new BigDecimal(0.01).setScale(2, BigDecimal.ROUND_HALF_UP);
        PENNY_STOCK_RECOVERY_MIRACLE_MULTIPLIER = new BigDecimal(600.0);
    }

    private static final BigDecimal orderFee = new BigDecimal("24.95");
    private static final BigDecimal cashFee = new BigDecimal("0.0");

    public static BigDecimal getOrderFee(String orderType) {
        if ((orderType.compareToIgnoreCase("BUY") == 0)
                || (orderType.compareToIgnoreCase("SELL") == 0)) {
            return orderFee;
        }
        return cashFee;
    }

    /* ------------------------------------------------------------------ */
    /* Random data generators (used by entity getRandomInstance helpers)   */
    /* ------------------------------------------------------------------ */

    public static double random() {
        return randomNumberGenerator.nextDouble();
    }

    public static int rndInt(int i) {
        return (int) (random() * i);
    }

    public static float rndFloat(int i) {
        return (float) (random() * i);
    }

    public static BigDecimal rndBigDecimal(float f) {
        return new BigDecimal(random() * f).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public static boolean rndBoolean() {
        return randomNumberGenerator.nextBoolean();
    }

    public static float rndQuantity() {
        return ((float) rndInt(200)) + 1.0f;
    }

    public static String rndSymbol() {
        return "s:" + rndInt(MAX_QUOTES - 1);
    }

    public static String rndAddress() {
        return rndInt(1000) + " Oak St.";
    }

    public static String rndCreditCard() {
        return rndInt(100) + "-" + rndInt(1000) + "-" + rndInt(1000) + "-" + rndInt(1000);
    }

    public static String rndEmail(String userID) {
        return userID + "@" + rndInt(100) + ".com";
    }

    public static String rndFullName() {
        return "first:" + rndInt(1000) + " last:" + rndInt(5000);
    }

    public static String rndUserID() {
        return getNextUserIDFromDeck();
    }

    private static ArrayList<Integer> deck = null;
    private static int card = 0;

    private static synchronized String getNextUserIDFromDeck() {
        int numUsers = getMAX_USERS();
        if (deck == null) {
            deck = new ArrayList<Integer>(numUsers);
            for (int i = 0; i < numUsers; i++) {
                deck.add(i, i);
            }
            java.util.Collections.shuffle(deck, randomNumberGenerator);
        }
        if (card >= numUsers) {
            card = 0;
        }
        return "uid:" + deck.get(card++);
    }

    private static final BigDecimal ONE = new BigDecimal(1.0);

    public static BigDecimal getRandomPriceChangeFactor() {
        // Vary change factor between 1.2 and 0.8 (DAYTRADER-25)
        double percentGain = rndFloat(1) * 0.2;
        if (random() < .5) {
            percentGain *= -1;
        }
        percentGain += 1;

        BigDecimal percentGainBD = new BigDecimal(percentGain).setScale(2, BigDecimal.ROUND_HALF_UP);
        if (percentGainBD.doubleValue() <= 0.0) {
            percentGainBD = ONE;
        }
        return percentGainBD;
    }

    public static int getMAX_USERS() {
        return MAX_USERS;
    }

    public static void setMAX_USERS(int maxUsers) {
        MAX_USERS = maxUsers;
    }

    public static int getMAX_QUOTES() {
        return MAX_QUOTES;
    }

    public static void setMAX_QUOTES(int maxQuotes) {
        MAX_QUOTES = maxQuotes;
    }

    public static boolean getUpdateQuotePrices() {
        return updateQuotePrices;
    }

    public static void setUpdateQuotePrices(boolean update) {
        updateQuotePrices = update;
    }

    public static boolean getPublishQuotePriceChange() {
        return publishQuotePriceChange;
    }

    public static void setPublishQuotePriceChange(boolean publish) {
        publishQuotePriceChange = publish;
    }

    public static int getMarketSummaryInterval() {
        return marketSummaryInterval;
    }

    public static void setMarketSummaryInterval(int seconds) {
        marketSummaryInterval = seconds;
    }
}
