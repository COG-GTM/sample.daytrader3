# Phase 7 — Configuration Migration

## Datasource & JPA

The legacy slice relied on a container-managed JTA datasource (`jdbc/TradeDataSource`)
declared in `persistence.xml` and the Liberty server config. That is replaced by
Spring Boot configuration in `daytrader3-springboot/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:h2:mem:tradedb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=none
spring.sql.init.mode=always
spring.jpa.open-in-view=false
```

An embedded in-memory H2 database is used for run/test so the module is runnable
with no external app server or database.

## SQL scripts

`src/main/resources/schema.sql` is ported **verbatim** from the legacy
`daytrader3-ee6-ejb/src/main/resources/META-INF/daytrader.sql` (with H2-friendly
`create table if not exists` / `create index if not exists`).

This is deliberate rather than letting Hibernate generate the schema: the legacy
`ORDEREJB.HOLDING_HOLDINGID` column is a **plain, non-unique indexed integer with no
foreign key** (`create index holding_holdingid on orderejb(holding_holdingid)`).
Hibernate's `@OneToOne` DDL generation would instead emit a `UNIQUE` constraint,
which breaks legitimate legacy behavior where the originating **buy** order and a
later **sell** order both reference the same holding before it is removed. Driving
the schema from `schema.sql` (with `ddl-auto=none`) preserves the original shape
and the original business behavior.

## Logging

Default Spring Boot logging (Logback); application package level set to INFO. The
legacy custom `Log` utility is retained for parity of trace messages.

## Verification checklist
- [x] Container datasource replaced by Spring datasource config (embedded H2).
- [x] Legacy SQL schema ported as `schema.sql`; schema shape preserved.
- [x] `ddl-auto=none` + `spring.sql.init.mode=always` verified by app startup & tests.
