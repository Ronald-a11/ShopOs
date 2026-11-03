# 1. Modular monolith on PostgreSQL

Date: 2026-10-29
Status: Accepted

## Decision

ShopOS ships as one Spring Boot application in front of one PostgreSQL
database. Modules live under `zw.miccs.shopos.<module>` and talk only through
their public `application` service interfaces. Microservices, message brokers
and distributed deployments are excluded until there are paying customers.

## Consequences

- One Docker image per commit, promoted from staging to production.
- Module boundaries are enforced by ArchUnit tests in CI rather than by
  network boundaries.

New decisions: copy this file as `NNNN-short-title.md`.
