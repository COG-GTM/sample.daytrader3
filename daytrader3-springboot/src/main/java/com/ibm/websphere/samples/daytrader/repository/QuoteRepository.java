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
package com.ibm.websphere.samples.daytrader.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ibm.websphere.samples.daytrader.domain.QuoteDataBean;

import jakarta.persistence.LockModeType;

/**
 * Spring Data JPA repository for {@link QuoteDataBean}, replacing the
 * EntityManager + named-query access used by the legacy EJB.
 */
public interface QuoteRepository extends JpaRepository<QuoteDataBean, String> {

    /** Replacement for the {@code quoteejb.quotesByChange} named query. */
    @Query("SELECT q FROM quoteejb q WHERE q.symbol LIKE 's:1__' ORDER BY q.change1 DESC")
    List<QuoteDataBean> findQuotesByChange();

    /**
     * Replacement for the {@code quoteejb.quoteForUpdate} native "select ... for
     * update" query: a pessimistic write lock for the read-modify-write pricing path.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT q FROM quoteejb q WHERE q.symbol = :symbol")
    Optional<QuoteDataBean> findBySymbolForUpdate(@Param("symbol") String symbol);
}
