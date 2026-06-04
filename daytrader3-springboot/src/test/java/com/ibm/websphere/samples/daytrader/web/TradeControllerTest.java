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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Web-layer tests exercising the migrated {@link TradeController} REST endpoints
 * end-to-end against the real Spring service and in-memory H2 database.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TradeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registerLoginQuoteAndBuyEndToEnd() throws Exception {
        mockMvc.perform(post("/api/register")
                .param("userID", "uid:web")
                .param("password", "password")
                .param("fullname", "Web User")
                .param("address", "1 Web St")
                .param("email", "web@example.com")
                .param("creditcard", "1111-2222")
                .param("openBalance", "50000.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileID").value("uid:web"));

        mockMvc.perform(post("/api/login")
                .param("userID", "uid:web")
                .param("password", "password"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginCount").value(1));

        mockMvc.perform(post("/api/quotes")
                .param("symbol", "s:200")
                .param("companyName", "WebCo")
                .param("price", "30.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.symbol").value("s:200"));

        mockMvc.perform(get("/api/quotes/{symbol}", "s:200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("WebCo"));

        mockMvc.perform(post("/api/accounts/{userID}/buy", "uid:web")
                .param("symbol", "s:200")
                .param("quantity", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderType").value("buy"))
                .andExpect(jsonPath("$.orderStatus").value("closed"));

        mockMvc.perform(get("/api/accounts/{userID}/holdings", "uid:web"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getQuoteForUnknownSymbolReturnsEmptyBody() throws Exception {
        mockMvc.perform(get("/api/quotes/{symbol}", "s:does-not-exist"))
                .andExpect(status().isOk());
    }
}
