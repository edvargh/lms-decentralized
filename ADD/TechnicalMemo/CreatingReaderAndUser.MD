# Technical Memo – Creating Reader and User in a Single Request

## Issue

Functional Requirement B states that a librarian must be able to create a Reader and the corresponding User in the same request.  
In the decentralized LMS architecture, user data is owned by the identity-service, while reader data is owned by the reader-lending-service, which introduces a cross-service coordination challenge.

---

## Problem

How to support creating a reader and its corresponding user atomically across two independent services, while preserving service ownership boundaries, avoiding shared databases, and maintaining system consistency in case of partial failure.

---

## Summary of Solution

The reader-lending-service acts as the orchestrator for reader creation.  
When a librarian creates a reader, the reader-lending-service first creates the corresponding user in the identity-service via a synchronous REST call. If local reader creation fails afterward, the reader-lending-service compensates by rolling back the user creation in the identity-service.

---

## Factors

- The system follows a decentralized architecture with separate databases per service.  
- Identity-related data (username, roles, authentication) is owned exclusively by the identity-service.  
- Reader-specific data (reader number, birth date, consents, interests, photo) is owned by the reader-lending-service.  
- Distributed transactions (e.g. two-phase commit) are intentionally avoided to keep the architecture simple.  
- All inter-service communication is performed via REST with JWT-based authorization.  

---

## Solution

- The reader-lending-service exposes a `POST /api/readers` endpoint restricted to librarians.  
- Upon receiving a create-reader request:  
  1. The reader-lending-service validates the request locally.  
  2. It sends a REST request to the identity-service to create the corresponding user.  
  3. If user creation succeeds, the reader-lending-service persists the reader locally.  
  4. If local persistence fails, a compensating request is sent to the identity-service to delete the previously created user.  
- Hibernate schema validation issues caused by case-sensitive table names in Docker/MySQL were resolved to ensure reliable startup and correct persistence.  
- Flyway migrations were aligned exactly with Hibernate expectations (table names, column names, and collection tables).  

---

## Motivation

This solution enables the functional requirement without breaking service autonomy or introducing shared persistence.  
It provides a predictable and testable flow while keeping each service responsible only for its own data.  
The approach mirrors common microservice orchestration patterns and supports gradual system evolution.

---

## Alternatives

- Use a shared database between services.  
- Implement distributed transactions (2PC).  
- Allow partial creation (user without reader or reader without user).  
- Perform asynchronous eventual-consistency creation.  

---

## Justification

Orchestrating the process in the reader-lending-service keeps identity concerns isolated while allowing a single logical operation from the client’s perspective.  
The compensating rollback mechanism ensures system consistency without requiring complex transaction coordination.  
This approach respects bounded contexts, avoids tight coupling, and aligns with microservice best practices.

---

## Pending Issues

- Handling retries or idempotency if the client resubmits the same request.  
- Improving observability and tracing across services for cross-service operations.  
- Evaluating whether asynchronous creation would be beneficial under higher load.
