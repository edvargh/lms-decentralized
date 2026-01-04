# Technical Memo – Creating Book, Author, and Genre in a Single Request

## Issue

Functional Requirement B states that a librarian must be able to create a Book together with its Author(s) and Genre in the same process.  
In the decentralized LMS architecture, book-related domain concepts (Book, Author, Genre) are owned by the catalog-service. This requirement must be fulfilled without introducing cross-service dependencies or breaking service ownership boundaries.

---

## Problem

How to support creating a Book, its Author(s), and its Genre atomically within a decentralized architecture, while preserving clear service ownership, ensuring data consistency, and avoiding unnecessary inter-service communication.

---

## Summary of Solution

The requirement is implemented entirely within the catalog-service by introducing a compound create endpoint.  
Because Book, Author, and Genre are all owned by the same service and persisted in the same database, the entire operation is handled locally within a single database transaction. The service creates missing authors and genres if they do not already exist, then persists the book and establishes all relationships atomically.

---

## Factors

- The system follows a decentralized architecture with clear service ownership boundaries.  
- Book, Author, and Genre are part of the same bounded context and owned by the catalog-service.  
- The system avoids cross-service writes unless strictly necessary.  
- The catalog-service uses a single relational database managed through Flyway migrations.  
- Authorization is enforced using JWT-based role checks via the API gateway.  

---

## Solution

- The catalog-service exposes a `POST /api/books/{isbn}/compound` endpoint restricted to librarians.  
- Upon receiving a compound create request:  
  1. The request is authenticated and authorized (LIBRARIAN role required).  
  2. The service validates the incoming data.  
  3. The genre is looked up by name and created if it does not already exist.  
  4. Each author in the request is created if missing.  
  5. The book is persisted and linked to the resolved authors and genre.  
- The entire operation executes within a single transactional boundary, ensuring atomicity.  
- Primary key generation was aligned with MySQL semantics using IDENTITY to avoid Hibernate sequence table issues in Dockerized environments.  

---

## Motivation

This solution fulfills the functional requirement with minimal architectural complexity by leveraging proper service boundaries.  
By handling the operation entirely within the catalog-service, the design avoids unnecessary orchestration, reduces failure modes, and ensures strong consistency for related domain entities.  
The approach aligns with domain-driven design principles and supports a clean, maintainable architecture.

---

## Alternatives

- Require authors and genres to be created in separate requests beforehand.  
- Introduce a separate orchestration service.  
- Use asynchronous creation with eventual consistency.  
- Allow partial creation (book without authors or genre).  

---

## Justification

Handling the compound creation within a single service preserves service autonomy while providing a simple and intuitive API for clients.  
Because all involved entities belong to the same bounded context and database, local transactional handling is both sufficient and preferable to more complex coordination patterns.  
This approach minimizes coupling, reduces operational complexity, and aligns with microservice best practices.

---

## Pending Issues

- Handling idempotency if the same compound request is submitted multiple times.  
- Improving validation feedback for partially invalid author or genre data.  
- Evaluating whether bulk creation endpoints are needed under higher load.
