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
package com.ibm.websphere.samples.daytrader.web;

import java.math.BigDecimal;
import java.util.Collection;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ibm.websphere.samples.daytrader.TradeServices;
import com.ibm.websphere.samples.daytrader.domain.AccountDataBean;
import com.ibm.websphere.samples.daytrader.domain.AccountProfileDataBean;
import com.ibm.websphere.samples.daytrader.domain.HoldingDataBean;
import com.ibm.websphere.samples.daytrader.domain.OrderDataBean;
import com.ibm.websphere.samples.daytrader.domain.QuoteDataBean;
import com.ibm.websphere.samples.daytrader.dto.MarketSummaryDataBean;
import com.ibm.websphere.samples.daytrader.util.TradeConfig;

/**
 * Spring MVC {@code @RestController} exposing the migrated trade-services slice.
 * Each endpoint delegates to the {@link TradeServices} Spring bean, replacing the
 * legacy JSP/servlet + EJB front end while keeping the DTO shapes stable.
 */
@RestController
@RequestMapping("/api")
public class TradeController {

    private final TradeServices tradeService;

    public TradeController(TradeServices tradeService) {
        this.tradeService = tradeService;
    }

    @GetMapping("/marketSummary")
    public MarketSummaryDataBean getMarketSummary() throws Exception {
        return tradeService.getMarketSummary();
    }

    // ---- Authentication / account lifecycle ----

    @PostMapping("/login")
    public AccountDataBean login(@RequestParam String userID, @RequestParam String password) throws Exception {
        return tradeService.login(userID, password);
    }

    @PostMapping("/logout/{userID}")
    public ResponseEntity<Void> logout(@PathVariable String userID) throws Exception {
        tradeService.logout(userID);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public AccountDataBean register(@RequestParam String userID,
            @RequestParam String password,
            @RequestParam String fullname,
            @RequestParam String address,
            @RequestParam String email,
            @RequestParam String creditcard,
            @RequestParam BigDecimal openBalance) throws Exception {
        return tradeService.register(userID, password, fullname, address, email, creditcard, openBalance);
    }

    // ---- Account / portfolio ----

    @GetMapping("/accounts/{userID}")
    public AccountDataBean getAccountData(@PathVariable String userID) throws Exception {
        return tradeService.getAccountData(userID);
    }

    @GetMapping("/accounts/{userID}/profile")
    public AccountProfileDataBean getAccountProfileData(@PathVariable String userID) throws Exception {
        return tradeService.getAccountProfileData(userID);
    }

    @PutMapping("/accounts/profile")
    public AccountProfileDataBean updateAccountProfile(@RequestBody AccountProfileDataBean profileData)
            throws Exception {
        return tradeService.updateAccountProfile(profileData);
    }

    @GetMapping("/accounts/{userID}/holdings")
    public Collection<?> getHoldings(@PathVariable String userID) throws Exception {
        return tradeService.getHoldings(userID);
    }

    @GetMapping("/holdings/{holdingID}")
    public HoldingDataBean getHolding(@PathVariable Integer holdingID) throws Exception {
        return tradeService.getHolding(holdingID);
    }

    @GetMapping("/accounts/{userID}/orders")
    public Collection<?> getOrders(@PathVariable String userID) throws Exception {
        return tradeService.getOrders(userID);
    }

    @GetMapping("/accounts/{userID}/closedOrders")
    public Collection<?> getClosedOrders(@PathVariable String userID) throws Exception {
        return tradeService.getClosedOrders(userID);
    }

    // ---- Trading actions ----

    @PostMapping("/accounts/{userID}/buy")
    public OrderDataBean buy(@PathVariable String userID,
            @RequestParam String symbol,
            @RequestParam double quantity) throws Exception {
        return tradeService.buy(userID, symbol, quantity, TradeConfig.SYNCH);
    }

    @PostMapping("/accounts/{userID}/sell")
    public OrderDataBean sell(@PathVariable String userID,
            @RequestParam Integer holdingID) throws Exception {
        return tradeService.sell(userID, holdingID, TradeConfig.SYNCH);
    }

    // ---- Quotes ----

    @GetMapping("/quotes")
    public Collection<?> getAllQuotes() throws Exception {
        return tradeService.getAllQuotes();
    }

    @GetMapping("/quotes/{symbol}")
    public QuoteDataBean getQuote(@PathVariable String symbol) throws Exception {
        return tradeService.getQuote(symbol);
    }

    @PostMapping("/quotes")
    public QuoteDataBean createQuote(@RequestParam String symbol,
            @RequestParam String companyName,
            @RequestParam BigDecimal price) throws Exception {
        return tradeService.createQuote(symbol, companyName, price);
    }
}
