# Phase 0 — Pre-Flight Analysis

Migration: **DayTrader3 trade-services slice — Java EE 6 (EJB/JPA on WebSphere Liberty) → Spring Boot 3.3.5 on Java 21 (Jakarta EE 10)**

## Build tool detection (REQ-P0-1.1)

The repository supports **both Maven and Gradle**:
- Maven aggregator `pom.xml` (parent `net.wasdev.maven.parent:java7-parent:1.3`) with modules: `daytrader3-ee6-ejb`, `daytrader3-ee6-rest`, `daytrader3-ee6-web`, `daytrader3-ee6`, `daytrader3-ee6-wlpcfg`.
- Gradle (`build.gradle`, `settings.gradle`, `gradle.properties`) targeting the same modules + a Liberty install.
- `.travis.yml` builds with `openjdk8` via both `gradle clean build` and `mvn clean install`.

The migrated slice uses **Maven** (Spring Boot's first-class build tool) in a new, self-contained module.

## Custom build settings (REQ-P0-1.2)

- No `maven-settings.xml` / custom `settings.xml` in the repo.
- No `.mvn/` wrapper. No `mvnw`/`gradlew` committed.
- Legacy build depends on a **WebSphere Liberty install** (`libertyRoot` in `gradle.properties`, `daytrader3-ee6-wlpcfg`), which is **not present** in this environment, so the legacy reactor cannot be built/run here.

## Current state baseline (REQ-P0-3.1)

| Property | Current (legacy) | Target |
|---|---|---|
| Platform | Java EE 6 (EJB 3.0, JPA 2.0, JMS 1.1, JTA 1.1) | Jakarta EE 10 via Spring Boot 3.3.5 |
| Namespace | `javax.*` | `jakarta.*` |
| Spring Boot | none (container-managed) | 3.3.5 |
| JDK | 8 (build), 7 (parent enforce) | 21 |
| Runtime | WebSphere Liberty app server | embedded Tomcat (Spring Boot) |
| Persistence | container JPA (`persistence.xml`, JTA datasource `jdbc/TradeDataSource`) | Spring Data JPA + Hibernate |
| Transactions | container-managed (`@TransactionAttribute`) | Spring `@Transactional` |
| Build | Maven + Gradle, Liberty plugin | Maven + spring-boot-starter-parent |

There is **no pre-existing Spring Boot version** to bump — this is a framework/platform migration, not a version upgrade. The playbook phases are applied in spirit (artifacts, stop gates, no test shortcuts, namespace migration, tests must pass, PR) to a greenfield Spring Boot module that re-implements the trade-services behavior.

## Dependency snapshot (REQ-P0-2.1)

Legacy `daytrader3-ee6-ejb` provided-scope dependencies (the trade-services slice):
- `javax.jms:jms-api:1.1-rev-1`
- `javax.ejb:ejb-api:3.0`
- `org.hibernate.javax.persistence:hibernate-jpa-2.0-api:1.0.1.Final`
- `javax.transaction:jta:1.1`

A full `mvn dependency:tree` against the legacy reactor is **not reproducible** in this environment because the build requires a Liberty install (see above). The new module's resolved tree is captured in Phase 9 (`09-dependency-tree.txt`).

## Existing tests baseline (Phase 1 input)

`find . -path '*/src/test/*' -name '*.java'` → **0 test files**. The legacy benchmark ships **no automated tests**; load is driven externally via JMeter (`jmeter_files/`). Baseline test count = **0**. New tests are written for the migrated slice (net increase, satisfying "test count must not decrease").

## Trade-services slice inventory (in scope)

Contract: `TradeServices` (interface).
Entities (JPA): `AccountDataBean`, `AccountProfileDataBean`, `HoldingDataBean`, `OrderDataBean`, `QuoteDataBean`.
DTO/value: `MarketSummaryDataBean`, `RunStatsDataBean`.
Logic: `TradeSLSBBean` (EJB SLSB, JPA/EntityManager impl) — primary port source.
Support: `TradeConfig` (constants + RNG helpers), `util/FinancialUtils`.

### Deferred (out of scope, documented in MIGRATION_SUMMARY.md)
- JMS/async: `queueOrder`, `publishQuotePriceChange`, `ASYNCH_2PHASE` path, `ejb3/DTBroker3MDB`, `ejb3/DTStreamer3MDB`, `pingTwoPhase`.
- `direct/TradeDirect` (raw JDBC alternate impl), `direct/KeySequenceDirect`, `DirectSLSBBean`.
- Web/JSP UI (`daytrader3-ee6-web`), JAX-RS module (`daytrader3-ee6-rest`), `resetTrade` bulk/DB-reset primitive.
