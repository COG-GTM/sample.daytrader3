# Phase 6 — Namespace Migration (javax.* → jakarta.*)

## Approach

Spring Boot 3.x baselines on **Jakarta EE 10**, so every EE API the trade-services
slice touches (`javax.persistence`, `javax.ejb`, etc.) maps to the `jakarta.*`
namespace. The migrated module (`daytrader3-springboot/`) was authored directly
against `jakarta.*`, and the legacy `daytrader3-ee6-*` modules are intentionally
left untouched (out of scope for this slice).

## Standard tool

The canonical automated tool is OpenRewrite's
`org.openrewrite.java.migrate.jakarta.JavaxMigrationToJakarta` recipe (or the
Eclipse Transformer). It performs a package rename `javax.* → jakarta.*` across a
codebase. Because the new module contains **no `javax.*` imports to begin with**,
running the recipe against it is a no-op; the rename was applied at authoring time
as the entities/services were ported.

EJB-specific APIs (`javax.ejb.*`, `@Stateless`, `@TransactionAttribute`) have **no
Jakarta equivalent in the Spring model** — they are replaced by Spring stereotypes
(`@Service`, `@Transactional`) rather than renamed. See
`05-framework-and-persistence.md`.

## Verification (evidence)

```
$ grep -rn "import javax\." daytrader3-springboot/src/ | wc -l
0
$ grep -rn "import jakarta\." daytrader3-springboot/src/ | wc -l
65
```

All 65 EE imports in the migrated slice are `jakarta.persistence.*`; zero `javax.*`
imports remain.

| Legacy (javax) | Migrated (jakarta / Spring) |
|---|---|
| `javax.persistence.*` (Entity, Id, NamedQuery, EntityManager, …) | `jakarta.persistence.*` + Spring Data JPA repositories |
| `javax.ejb.Stateless`, `@TransactionAttribute` | `@org.springframework.stereotype.Service`, `@org.springframework.transaction.annotation.Transactional` |
| `javax.ejb.EJBException` | `com.ibm.websphere.samples.daytrader.util.TradeException` (unchecked, triggers rollback) |
| `javax.jms.*`, `javax.annotation.Resource` (queues/topics) | **Deferred** — see `MIGRATION_SUMMARY.md` |

## Verification checklist
- [x] Standard tool identified (OpenRewrite JavaxMigrationToJakarta).
- [x] Zero `javax.*` imports remain in the migrated module (grep evidence above).
- [x] EE-only constructs (EJB/JMS) mapped to Spring equivalents or explicitly deferred.
