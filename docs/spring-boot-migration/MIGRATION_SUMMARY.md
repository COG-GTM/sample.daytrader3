# DayTrader3 — Trade-Services Migration to Spring Boot 3

Migrates the **trade-services slice** of DayTrader3 from Java EE 6 (EJB/JPA on
WebSphere Liberty) to **Spring Boot 3.3.5 on Java 21 (Jakarta EE 10)**, as a
self-contained proof of the end-to-end pattern. The legacy `daytrader3-ee6-*`
modules are left untouched.

## What changed

| Concern | Before (Java EE 6) | After (Spring Boot 3) |
|---|---|---|
| Services | `TradeSLSBBean` `@Stateless` EJB | `TradeServiceImpl` `@Service` |
| Endpoints | JSP/servlet front end | `TradeController` `@RestController` (`/api`) |
| Persistence | container JPA + `EntityManager` | Spring Data JPA repositories |
| Transactions | container-managed `@TransactionAttribute` | Spring `@Transactional` |
| Namespace | `javax.*` | `jakarta.*` |
| Runtime | WebSphere Liberty | embedded Tomcat (no app-server dep) |
| Datasource | JTA `jdbc/TradeDataSource` | embedded H2 + `schema.sql` |

New module: **`daytrader3-springboot/`** (Spring Boot `3.3.5`, Java 21).

## Preserved behavior & contracts

- The `TradeServices` interface (method signatures) and all DTO/entity shapes
  (`QuoteDataBean`, `OrderDataBean`, `HoldingDataBean`, `AccountDataBean`,
  `AccountProfileDataBean`, `MarketSummaryDataBean`, `RunStatsDataBean`) are kept
  stable, so JSON contracts and business semantics match the legacy beans.
- Synchronous trade actions (buy/sell/complete/quote pricing) are ported verbatim,
  including order fees, balance debit/credit, pessimistic locking on quote price
  updates, and TSIA/top-mover market-summary computation.
- The legacy DB schema is reproduced exactly (`schema.sql`) — notably the
  non-unique `ORDEREJB.HOLDING_HOLDINGID` column, which is required for the
  buy-then-sell lifecycle.

## Deferred: JMS / async paths (out of scope for this slice)

The following depend on JMS (queues/topics/MDBs) and JTA two-phase commit, which
are not part of the trade-services slice and are deferred to a later PR:

| Capability | Legacy mechanism | Status in this PR |
|---|---|---|
| Async order processing | `queueOrder()` → `TradeBrokerQueue` MDB | `UnsupportedOperationException`; use `orderProcessingMode = SYNCH` |
| Quote price-change publishing | `publishQuotePriceChange()` → `TradeStreamerTopic` | no-op honoring the `publishQuotePriceChange` flag; throws if enabled |
| Two-phase ping | `pingTwoPhase()` | not ported |
| Bulk benchmark reset | `resetTrade()` (direct JDBC) | `UnsupportedOperationException` |
| `orderCompleted()` callback | MDB completion callback | `UnsupportedOperationException` (matches legacy SLSB) |

**Why deferred:** these require a JMS broker (e.g. Artemis) and an async worker
model; wiring them in would expand the slice well beyond the quote/order/account
core and pull in messaging infrastructure. The synchronous completion path
(`completeOrder`) fully exercises the buy/sell business logic without JMS.

## Build & test

```
cd daytrader3-springboot
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn clean test
```

- **13/13 tests pass** (service + web layers) against embedded H2.
- Application boots on embedded Tomcat (`mvn spring-boot:run`) and serves the REST
  API; smoke-tested register → quote → buy → holdings.
- Legacy baseline was 0 runnable tests (reactor needs a Liberty install), so test
  count did not regress.

See `docs/spring-boot-migration/artifacts/` for per-phase details
(pre-flight, framework/persistence, namespace, configuration, build/test).

## Follow-ups (suggested next PRs)
1. Port the JMS async paths (order queue + quote streamer) onto a Spring JMS broker.
2. Migrate the remaining web tier (JSF/servlets) and `TradeDirect` JDBC path.
3. Add a production datasource profile (e.g. Derby/DB2) alongside the H2 dev profile.
