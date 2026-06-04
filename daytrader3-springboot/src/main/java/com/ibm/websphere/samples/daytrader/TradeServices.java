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

import java.math.BigDecimal;
import java.util.Collection;

import com.ibm.websphere.samples.daytrader.domain.AccountDataBean;
import com.ibm.websphere.samples.daytrader.domain.AccountProfileDataBean;
import com.ibm.websphere.samples.daytrader.domain.HoldingDataBean;
import com.ibm.websphere.samples.daytrader.domain.OrderDataBean;
import com.ibm.websphere.samples.daytrader.domain.QuoteDataBean;
import com.ibm.websphere.samples.daytrader.dto.MarketSummaryDataBean;
import com.ibm.websphere.samples.daytrader.dto.RunStatsDataBean;

/**
 * TradeServices interface specifies the business methods provided by the Trade
 * online broker application. Preserved verbatim from the Java EE 6 contract so
 * the migrated Spring {@code @Service} implementation keeps the same shape.
 */
public interface TradeServices {

    MarketSummaryDataBean getMarketSummary() throws Exception;

    OrderDataBean buy(String userID, String symbol, double quantity, int orderProcessingMode) throws Exception;

    OrderDataBean sell(String userID, Integer holdingID, int orderProcessingMode) throws Exception;

    void queueOrder(Integer orderID, boolean twoPhase) throws Exception;

    OrderDataBean completeOrder(Integer orderID, boolean twoPhase) throws Exception;

    void cancelOrder(Integer orderID, boolean twoPhase) throws Exception;

    void orderCompleted(String userID, Integer orderID) throws Exception;

    Collection<?> getOrders(String userID) throws Exception;

    Collection<?> getClosedOrders(String userID) throws Exception;

    QuoteDataBean createQuote(String symbol, String companyName, BigDecimal price) throws Exception;

    QuoteDataBean getQuote(String symbol) throws Exception;

    Collection<?> getAllQuotes() throws Exception;

    QuoteDataBean updateQuotePriceVolume(String symbol, BigDecimal newPrice, double sharesTraded) throws Exception;

    Collection<?> getHoldings(String userID) throws Exception;

    HoldingDataBean getHolding(Integer holdingID) throws Exception;

    AccountDataBean getAccountData(String userID) throws Exception;

    AccountProfileDataBean getAccountProfileData(String userID) throws Exception;

    AccountProfileDataBean updateAccountProfile(AccountProfileDataBean profileData) throws Exception;

    AccountDataBean login(String userID, String password) throws Exception;

    void logout(String userID) throws Exception;

    AccountDataBean register(String userID, String password, String fullname, String address, String email,
            String creditcard, BigDecimal openBalance) throws Exception;

    RunStatsDataBean resetTrade(boolean deleteAll) throws Exception;
}
