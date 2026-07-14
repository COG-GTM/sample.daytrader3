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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.Date;

import javax.ejb.EJBException;

import org.junit.jupiter.api.Test;

/**
 * Regression tests for the login/logout behaviour exposed to users.
 */
public class AccountDataBeanTest {

    private AccountDataBean accountWithProfile(String userId, String password) {
        AccountDataBean account = new AccountDataBean(0, 0, new Date(), new Date(),
                new BigDecimal("1000000"), new BigDecimal("1000000"), userId);
        AccountProfileDataBean profile =
                new AccountProfileDataBean(userId, password, "full name", "addr", "e@mail", "1-2-3-4");
        account.setProfile(profile);
        return account;
    }

    @Test
    public void successfulLoginIncrementsLoginCountAndSetsLastLogin() {
        AccountDataBean account = accountWithProfile("uid:0", "secret");
        account.login("secret");
        assertEquals(1, account.getLoginCount());
        assertNotNull(account.getLastLogin());
    }

    @Test
    public void wrongPasswordThrowsAndDoesNotIncrementLogin() {
        AccountDataBean account = accountWithProfile("uid:0", "secret");
        assertThrows(EJBException.class, () -> account.login("wrong"));
        assertEquals(0, account.getLoginCount());
    }

    @Test
    public void missingProfileThrowsOnLogin() {
        AccountDataBean account = new AccountDataBean(0, 0, new Date(), new Date(),
                new BigDecimal("1000000"), new BigDecimal("1000000"), "uid:0");
        assertThrows(EJBException.class, () -> account.login("secret"));
    }

    @Test
    public void logoutIncrementsLogoutCount() {
        AccountDataBean account = accountWithProfile("uid:0", "secret");
        account.logout();
        account.logout();
        assertEquals(2, account.getLogoutCount());
    }
}
