# DayTrader3 — Java 8 → Java 21 Migration Plan

Jira: **JEFF-DT3-JAVA21** (Java 21 upgrade for DayTrader3)

This document is the written migration plan produced during repository discovery, before
code changes. It captures the current state, the Java 8 / Java EE 6 assumptions that break
on a modern JDK, the required vs. optional work, and the high-risk areas on the trade path.

## 1. Architecture summary (as-is)

DayTrader3 is a Java EE 6 benchmark modelling an online stock-trading system (login,
portfolio, quotes, buy/sell orders, holdings). It is a Maven **and** Gradle multi-module
reactor targeting IBM WebSphere Liberty.

| Module | Packaging | Role |
| --- | --- | --- |
| `daytrader3-ee6-ejb` | jar (`dt-ejb.jar`) | Core business logic + JPA entities. EJB session beans (`TradeSLSBBean`), MDBs, and a JDBC "direct" mode (`TradeDirect`). Domain beans: `AccountDataBean`, `AccountProfileDataBean`, `QuoteDataBean`, `HoldingDataBean`, `OrderDataBean`, `MarketSummaryDataBean`; utilities `FinancialUtils`, `TradeConfig`. |
| `daytrader3-ee6-web` | war (`web.war`) | Servlets, JSPs, JSF managed beans (portfolio/quote/account UI). |
| `daytrader3-ee6-rest` | war (`Rest.war`) | JAX-RS (JSR-311) sample address-book endpoints using JAXB binding. |
| `daytrader3-ee6` | ear | Assembles the EJB jar + two wars; copies the Derby driver; wires the Liberty plugin for functional tests. |
| `daytrader3-ee6-wlpcfg` | pom | Liberty server configuration (`server.xml`, features). |

Runtime data store: **Apache Derby** (embedded), shipped as a copied driver jar.

Persistence/transactions are container-managed (JPA 2.0 + JTA 1.1). Messaging uses JMS 1.1
MDBs. All `javax.*` EE APIs (`javax.ejb`, `javax.jms`, `javax.persistence`, `javax.servlet`,
`javax.faces`, `javax.ws.rs`) are supplied by the container at runtime and by `provided`
Maven dependencies at compile time — they are **not** part of the JDK, so JDK removals do
not affect them.

## 2. Java 8 / legacy assumptions that break on Java 21

| # | Assumption | Impact on Java 21 | Required action |
| --- | --- | --- | --- |
| A1 | Compiler `source/target = 1.7` (Maven parent `java7-parent`; Gradle `sourceCompatibility = 1.7`) | javac 21 no longer supports `-source/-target 7` (min is 8). Build fails immediately. | Set release/source/target to **21** in Maven and Gradle. |
| A2 | REST module imports `javax.xml.bind.*` (JAXB) | **JAXB was removed from the JDK in Java 11** (JEP 320, `java.se.ee`). Compile fails. | Add `javax.xml.bind:jaxb-api` (+ `jaxb-runtime` at runtime) as explicit dependencies, keeping the `javax` namespace so no source changes are needed. |
| A3 | `javax.annotation.*` (`@PostConstruct`, `@Resource`) in EJB + web modules | **Common Annotations removed from the JDK in Java 11** (JEP 320). Compile fails. | Add `javax.annotation:javax.annotation-api` as a `provided` dependency (namespace preserved). |
| A4 | Legacy Maven plugin versions inherited from `java7-parent` (compiler, surefire, ear, war) | Old plugin versions can fail under a Java 21 toolchain and cannot run JUnit 5. | Pin modern plugin versions via `pluginManagement` in the root POM. |
| A5 | Apache **Derby 10.10.1.1** (2013) | Runs on old JDKs only and carries known CVEs (see §5). Newer Derby lines require newer JDKs. | Upgrade to **Derby 10.17.1.0** (the current line, which requires Java 21). |
| A6 | CI pinned to `openjdk8` (Travis) | Does not exercise Java 21. | Add a GitHub Actions workflow building + testing on **Temurin 21**; keep Travis JDK aligned. |

Notes on things that are **fine** and deliberately left unchanged:
- Deprecated boxing constructors (`new Integer(...)`, `new BigDecimal(double)`, etc., ~30
  sites) still compile on Java 21 (deprecation warnings only). Rewriting them is optional
  style churn and is intentionally **out of scope** to protect trade-path semantics.
- No usage of removed CORBA / JAX-WS / `sun.*` internal APIs was found.
- The `javax` EE namespace is retained end-to-end (no Jakarta `jakarta.*` rename), because
  the target runtime (WebSphere Liberty EE6/`javaee-6.0`) expects `javax`. A Jakarta EE
  migration is a separate, larger effort and would change runtime behavior — out of scope.

## 3. Required migration work vs. optional refactoring

**Required (in this PR):**
1. Compiler/runtime targets → Java 21 (Maven + Gradle).
2. Add JDK-removed EE APIs back as explicit dependencies (JAXB, Common Annotations).
3. Modern Maven plugin versions (compiler, surefire, ear, war).
4. Derby 10.10.1.1 → 10.17.1.0 (CVE remediation + Java 21 support).
5. Regression tests (none exist today) around order creation, quote retrieval, buy/sell
   execution math, and holdings valuation.
6. CI on Java 21; refreshed docs.

**Optional (explicitly deferred, not done here):**
- Jakarta EE 9+ (`javax.*` → `jakarta.*`) namespace migration.
- Replacing deprecated boxing/`BigDecimal(double)` constructors.
- Replacing the raw-JDBC "direct" mode or the JMS MDB workload.
- Virtual threads / records / other Java 21 language adoption in domain code.

## 4. High-risk, trade-path changes to review carefully

- **Derby major upgrade (10.10 → 10.17).** Derby 10.15+ is modularized (`derby`,
  `derbyshared`, `derbytools`, `derbyclient`). The embedded driver class and SQL dialect
  used by `TradeDirect` are unchanged, but the runtime now needs the companion jars on the
  server classpath. Money math (balances, holdings, order fees) must be re-verified against
  a running server. This is the single highest-risk item for externally observable behavior.
- **Order fee / balance arithmetic.** `TradeConfig.getOrderFee` (flat `24.95` for BUY/SELL)
  and `FinancialUtils` gain/holdings computations drive user-visible P&L. Covered by new
  regression tests; must not change.
- **JAXB provider at runtime.** The REST address-book sample relies on a JAXB
  implementation being present; on a modular JDK it must come from a dependency, not the
  platform. Low business impact (sample endpoint), but verify the REST war still deploys.

## 5. Security findings addressed

Apache Derby 10.10.1.1 → 10.17.1.0 remediates:
- **CVE-2015-1832** — XXE in Derby (fixed 10.12.1.1).
- **CVE-2018-1313** — information disclosure via crafted SQL (fixed 10.14.2.0).
- **CVE-2022-46337** — LDAP-injection / auth bypass, "alwaysAllow" (fixed 10.16.1.1).

10.17.1.0 is at or above every fixed line above.

## 6. Verification strategy

- `mvn clean package` on JDK 21 compiles all modules, runs the new JUnit 5 unit tests, and
  builds the jar/wars/ear — **without** requiring a Liberty download (the Liberty plugin
  goals are bound to the integration-test phases, which `package` does not reach).
- Full end-to-end trade-path validation (login → quote → buy → portfolio → sell) runs
  against a live Liberty + Derby server and is **not** part of unit CI; it remains a manual
  functional test (see README) and is called out in the human-review checklist.

## 7. Rollback guidance

The change set is confined to build files (`pom.xml`, `build.gradle`), a Derby version bump,
added test sources, CI config, and docs — no domain/business logic is modified.
- To roll back: revert this PR (single squashed commit) and redeploy the previous EAR built
  on Java 8; the schema and Derby data format are unchanged, so no data migration is needed.
- Partial rollback: if only Derby is problematic, revert the `derby.version` property to the
  prior value while keeping the Java 21 compiler settings.
