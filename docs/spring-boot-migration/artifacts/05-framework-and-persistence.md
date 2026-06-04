# Phase 5 — Framework & Persistence Migration

Converts the EJB-based trade-services slice to Spring Boot 3.3.5: EJBs → Spring
`@Service`, container-managed JPA → Spring Data JPA, CMT → Spring `@Transactional`.

## New module

`daytrader3-springboot/` — a self-contained Maven module using
`spring-boot-starter-parent:3.3.5` (Java 21), `spring-boot-starter-web`,
`spring-boot-starter-data-jpa`, and the H2 driver. It is **not** added to the
legacy Maven/Gradle reactor (which is pinned to `java7-parent` and needs a
WebSphere Liberty install), so the legacy build stays intact.

## Domain entities (jakarta.persistence)

| Entity | Table/Entity name | Notes |
|---|---|---|
| `QuoteDataBean` | `quoteejb` | named queries preserved; `quoteForUpdate` → pessimistic lock |
| `AccountDataBean` | `accountejb` | owning `@OneToOne` → profile (`PROFILE_USERID`) |
| `AccountProfileDataBean` | `accountprofileejb` | inverse `@OneToOne(mappedBy="profile", cascade=ALL)` |
| `HoldingDataBean` | `holdingejb` | `@ManyToOne` account/quote; TABLE id generator |
| `OrderDataBean` | `orderejb` | `@OneToOne` holding (non-unique col, see Phase 7) |

DTOs (non-entities): `MarketSummaryDataBean`, `RunStatsDataBean`. IDs use
`@TableGenerator` against `KEYGENEJB` (`KEYNAME`/`KEYVAL`), matching the legacy
key-generation table.

## Spring Data JPA repositories

Replace direct `EntityManager` access from `TradeSLSBBean`:

- `QuoteRepository` — `findQuotesByChange()` (legacy `quoteejb.quotesByChange`),
  `findBySymbolForUpdate()` with `@Lock(PESSIMISTIC_WRITE)` (legacy `select … for update`).
- `HoldingRepository` — `findByUserID()` (legacy `holdingejb.holdingsByUserID`).
- `OrderRepository` — `findByUserID()`, `findClosedOrders()` (legacy `orderejb.closedOrders`).
- `AccountRepository`, `AccountProfileRepository` — CRUD by id.

## Service layer

`TradeServiceImpl implements TradeServices` (`@Service @Transactional`) ports the
synchronous business logic of `TradeSLSBBean` verbatim:
`getMarketSummary`, `buy`, `sell`, `completeOrder`, `cancelOrder`, `getOrders`,
`getClosedOrders`, `createQuote`, `getQuote`, `getAllQuotes`,
`updateQuotePriceVolume`, `getHoldings`, `getHolding`, `getAccountData`,
`getAccountProfileData`, `updateAccountProfile`, `login`, `logout`, `register`.

- Container-managed transactions → method-level `@Transactional`; read paths are
  `@Transactional(readOnly = true)`.
- `EJBException` → `TradeException` (unchecked) so rollback semantics are preserved.
- The `TradeServices` interface contract (method signatures, DTO shapes) is unchanged.

## REST layer

`TradeController` (`@RestController`, `/api`) exposes the slice over Spring MVC,
replacing the legacy JSP/servlet front end — e.g. `POST /api/accounts/{userID}/buy`,
`POST /api/accounts/{userID}/sell`, `GET /api/quotes`, `GET /api/marketSummary`,
`POST /api/login`, `POST /api/register`.

## Verification checklist
- [x] EJBs converted to Spring `@Service` beans behind `@RestController` endpoints.
- [x] JPA entities + persistence migrated to Spring Data JPA repositories.
- [x] Container-managed transactions replaced with Spring `@Transactional`.
- [x] Module compiles (`mvn compile`) and application starts on embedded Tomcat.
