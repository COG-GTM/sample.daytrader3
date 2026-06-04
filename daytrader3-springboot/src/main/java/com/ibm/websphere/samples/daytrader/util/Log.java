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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lightweight logging facade preserving the original DayTrader {@code Log} API
 * surface used by the trade-services slice, delegating to SLF4J instead of the
 * legacy WebSphere trace facilities.
 */
public final class Log {

    private static final Logger LOGGER = LoggerFactory.getLogger("com.ibm.websphere.samples.daytrader");

    private Log() {
    }

    public static boolean doTrace() {
        return LOGGER.isTraceEnabled();
    }

    public static boolean doActionTrace() {
        return LOGGER.isTraceEnabled();
    }

    public static void trace(String message) {
        LOGGER.trace(message);
    }

    public static void trace(String message, Object... parameters) {
        if (LOGGER.isTraceEnabled()) {
            StringBuilder sb = new StringBuilder(message).append('(');
            for (int i = 0; i < parameters.length; i++) {
                sb.append(parameters[i]);
                if (i < parameters.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append(')');
            LOGGER.trace(sb.toString());
        }
    }

    public static void log(String message) {
        LOGGER.info(message);
    }

    public static void log(String message, Throwable e) {
        LOGGER.info(message, e);
    }

    public static void error(String message) {
        LOGGER.error(message);
    }

    public static void error(String message, Throwable e) {
        LOGGER.error(message, e);
    }

    public static void error(Throwable e, String message) {
        LOGGER.error(message, e);
    }
}
