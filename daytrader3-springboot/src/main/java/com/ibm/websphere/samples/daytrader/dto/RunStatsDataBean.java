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
package com.ibm.websphere.samples.daytrader.dto;

import java.io.Serializable;

public class RunStatsDataBean implements Serializable {

    private static final long serialVersionUID = 4017778674103242167L;

    public RunStatsDataBean() {
    }

    // count of trade users in the database (users w/ userID like 'uid:%')
    private int tradeUserCount;
    // count of trade stocks in the database (stocks w/ symbol like 's:%')
    private int tradeStockCount;
    // count of new registered users in this run (users w/ userID like 'ru:%')
    private int newUserCount;
    // sum of logins by trade users
    private int sumLoginCount;
    // sum of logouts by trade users
    private int sumLogoutCount;
    // count of holdings of trade users
    private int holdingCount;
    // count of orders of trade users
    private int orderCount;
    // count of buy orders of trade users
    private int buyOrderCount;
    // count of sell orders of trade users
    private int sellOrderCount;
    // count of cancelled orders of trade users
    private int cancelledOrderCount;
    // count of open orders of trade users
    private int openOrderCount;
    // count of orders deleted during this trade Reset
    private int deletedOrderCount;

    public String toString() {
        return "\n\tRunStatsData for reset at " + new java.util.Date()
            + "\n\t\t      tradeUserCount: " + getTradeUserCount()
            + "\n\t\t        newUserCount: " + getNewUserCount()
            + "\n\t\t       sumLoginCount: " + getSumLoginCount()
            + "\n\t\t      sumLogoutCount: " + getSumLogoutCount()
            + "\n\t\t        holdingCount: " + getHoldingCount()
            + "\n\t\t          orderCount: " + getOrderCount()
            + "\n\t\t       buyOrderCount: " + getBuyOrderCount()
            + "\n\t\t      sellOrderCount: " + getSellOrderCount()
            + "\n\t\t cancelledOrderCount: " + getCancelledOrderCount()
            + "\n\t\t      openOrderCount: " + getOpenOrderCount()
            + "\n\t\t   deletedOrderCount: " + getDeletedOrderCount();
    }

    public int getTradeUserCount() {
        return tradeUserCount;
    }

    public void setTradeUserCount(int tradeUserCount) {
        this.tradeUserCount = tradeUserCount;
    }

    public int getNewUserCount() {
        return newUserCount;
    }

    public void setNewUserCount(int newUserCount) {
        this.newUserCount = newUserCount;
    }

    public int getSumLoginCount() {
        return sumLoginCount;
    }

    public void setSumLoginCount(int sumLoginCount) {
        this.sumLoginCount = sumLoginCount;
    }

    public int getSumLogoutCount() {
        return sumLogoutCount;
    }

    public void setSumLogoutCount(int sumLogoutCount) {
        this.sumLogoutCount = sumLogoutCount;
    }

    public int getHoldingCount() {
        return holdingCount;
    }

    public void setHoldingCount(int holdingCount) {
        this.holdingCount = holdingCount;
    }

    public int getBuyOrderCount() {
        return buyOrderCount;
    }

    public void setBuyOrderCount(int buyOrderCount) {
        this.buyOrderCount = buyOrderCount;
    }

    public int getSellOrderCount() {
        return sellOrderCount;
    }

    public void setSellOrderCount(int sellOrderCount) {
        this.sellOrderCount = sellOrderCount;
    }

    public int getCancelledOrderCount() {
        return cancelledOrderCount;
    }

    public void setCancelledOrderCount(int cancelledOrderCount) {
        this.cancelledOrderCount = cancelledOrderCount;
    }

    public int getOpenOrderCount() {
        return openOrderCount;
    }

    public void setOpenOrderCount(int openOrderCount) {
        this.openOrderCount = openOrderCount;
    }

    public int getDeletedOrderCount() {
        return deletedOrderCount;
    }

    public void setDeletedOrderCount(int deletedOrderCount) {
        this.deletedOrderCount = deletedOrderCount;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    public int getTradeStockCount() {
        return tradeStockCount;
    }

    public void setTradeStockCount(int tradeStockCount) {
        this.tradeStockCount = tradeStockCount;
    }
}
