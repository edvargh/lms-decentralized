# Technical Memo 04 – Controlled Database Migrations in Multi-Instance Services

## Issue

When running multiple instances of the same service, database schema migrations must be applied exactly once. Allowing each instance to manage migrations independently can lead to race conditions, startup failures, or inconsistent schemas.

---

## Problem

How to manage database schema creation and migration safely when multiple instances of a service start concurrently and share a single database.

---

## Summary of Solution

Introduce a dedicated Flyway migration container per service that executes database migrations once before any service instances start. Disable Flyway execution inside the application containers.

---

## Factors

Each service owns its own database and may be scaled horizontally using Docker Compose. All service instances connect to the same database and start concurrently. Schema initialization must therefore be deterministic and coordinated.

---

## Solution

- For each service (identity, catalog, reader-lending), a separate Flyway container is defined in Docker Compose.  
- The Flyway container runs database migrations on startup and exits successfully once migrations are complete.  
- Service containers depend on the successful completion of their corresponding Flyway container before starting.  
- Flyway is explicitly disabled inside the Spring Boot applications to prevent concurrent migration attempts.  

---

## Motivation

This approach ensures that database schema management is reliable, repeatable, and independent of service scaling.  
It removes startup race conditions and allows any number of service instances to start safely against a known-good schema.

---

## Alternatives

- Allow each service instance to run Flyway migrations on startup.  
- Manually manage database schemas outside the deployment process.  
- Use application-level locks to coordinate migrations.  

---

## Justification

Database schema changes are a shared, write-sensitive operation and must be performed exactly once per deployment. By separating migration execution into a single, dedicated container per service, schema initialization becomes deterministic and independent of service instance count. This enables safe horizontal scaling while preserving clear ownership of database evolution.

---

## Pending Issues

- How to handle coordinated schema migrations across services if cross-service database dependencies are introduced.  
- Whether migration execution should be integrated into a CI/CD pipeline for non-local environments.
