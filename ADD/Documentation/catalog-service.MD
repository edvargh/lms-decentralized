# ADD Second Iteration – Migration of Catalog-Service

## 4.2 Step 2 of ADD: Choose an Element of the System to Decompose

In the second iteration of the ADD process, the chosen element to decompose is the Catalog Service.  
After completing the migration of the identity-service in the first iteration, the catalog-service was selected as the next architectural element because it represents a core business capability of the LMS and contains significant domain logic related to books, authors, and genres.  
The catalog-service was previously part of a monolithic architecture and has now been migrated as an independent service. This iteration focuses on validating that the migrated service is functionally complete, correctly integrated with the identity-service, and ready to serve as a stable backend service in the decentralized LMS architecture.

---

## 4.3 Step 3 of ADD: Identify Candidate Architectural Drivers

The main architectural drivers influencing the migration of the catalog-service are:

- Modularity: The catalog-service must be clearly separated from other services and encapsulate all catalog-related responsibilities.  
- Security: Access to catalog endpoints must be protected using JWT-based authentication provided by the identity-service.  
- Scalability: The service must be able to scale independently from other services.  
- Maintainability: The codebase should be easy to understand, test, and extend.  
- Interoperability: The service must expose stable REST APIs that can be consumed by other services and clients.  
- Data Ownership: The catalog-service must own and manage its own database schema.  

These drivers guided architectural and implementation decisions throughout the migration process.

---

## 4.4 Step 4 of ADD: Choose a Design Concept that Satisfies the Architectural Drivers

To satisfy the identified architectural drivers, the catalog-service was designed and implemented as an independent Spring Boot microservice with the following design concepts:

- Service-per-domain: All catalog-related logic (books, authors, genres) is handled exclusively by the catalog-service.  
- REST-based communication: The service exposes RESTful endpoints for all catalog operations.  
- Database per service: The catalog-service uses its own MySQL database, isolated from other services.  
- JWT-based security: Authentication and authorization are enforced using JWT tokens issued by the identity-service.  
- Clear API boundaries: Endpoints that depend on lending or reader data are explicitly stubbed and documented as future integrations.  

---

## 4.4.1 Step 4, Substep 1 of ADD: Identify Design Concerns

The main design concerns identified for the catalog-service are:

- Correct enforcement of role-based access control (LIBRARIAN vs READER).  
- Proper separation between implemented endpoints and stubbed endpoints.  
- Correct JPA mappings and database schema generation.  
- Consistent error handling and HTTP status codes.  
- Safe handling of concurrency using versioning and ETags.  
- Integration with shared infrastructure services such as file storage.  

---

## 4.4.2 Step 4, Substep 2 of ADD: List Alternative Patterns for Subordinate Concerns

Several architectural and design alternatives were considered:

- Monolithic persistence vs database per service  
- Direct database access by other services vs strict data ownership  
- Synchronous REST calls vs asynchronous messaging  
- Manual schema management vs JPA-managed schema  
- Centralized authentication logic vs delegated authentication using JWT  

---

## 4.4.3 Step 4, Substep 3 of ADD: Select Patterns from the List

The following patterns were selected:

- Database per service to ensure loose coupling and independent evolution.  
- RESTful APIs for synchronous communication.  
- JWT authentication delegated to the identity-service.  
- Optimistic locking using version fields and If-Match headers.  
- Explicit stubbing of endpoints that depend on future services.  

These patterns were chosen to align with the decentralized LMS architecture and the constraints of the project.

---

## 4.4.4 Step 4, Substep 4 of ADD: Determine Relationship Between Patterns and Drivers

The selected patterns directly support the architectural drivers:

- Modularity is achieved through service isolation and data ownership.  
- Security is ensured by JWT-based authorization.  
- Scalability is enabled by independent deployment and databases.  
- Maintainability is improved through clear responsibility boundaries.  
- Interoperability is supported by stable REST APIs.  

---

## 4.4.5 Step 4, Substep 5 of ADD: Capture Preliminary Architectural Views

At this stage, the catalog-service consists of:

- REST controllers for books, authors, and genres.  
- A dedicated MySQL database accessed via JPA.  
- Security filters validating JWT tokens issued by the identity-service.  
- Clearly defined API endpoints, with some endpoints intentionally stubbed.  

This represents a stable architectural snapshot of the service after migration.

---

## 4.4.6 Step 4.6 of ADD: Evaluate and Resolve Inconsistencies

During testing, several inconsistencies were identified and resolved:

- SQL queries referencing incorrect table names were corrected.  
- Database schema mismatches were identified and validated using MySQL inspection.  
- Missing data (genres, authors, books) was manually inserted to enable endpoint testing.  
- Stubbed endpoints were confirmed to return appropriate responses.  
- Security configuration was verified for both READER and LIBRARIAN roles.  

---

## 4.5 Step 5 of ADD: Instantiate Architectural Elements and Allocate Responsibilities

The following responsibilities were allocated:

- Catalog-Service: Owns all catalog-related logic and persistence.  
- Identity-Service: Handles authentication and token issuance.  
- MySQL (catalog_db): Stores books, authors, genres, and relationships.  
- FileStorageService: Manages book and author photos.  
- Controllers: Expose REST APIs and enforce access control.  
- Services: Contain business logic and transaction boundaries.  

---

## 4.6 Step 7 of ADD: Verify and Refine Requirements and Make Them Constraints

After migration and testing, the following constraints were confirmed:

- All catalog endpoints require valid JWT authentication.  
- The catalog-service must not depend on lending or reader data.  
- Stubbed endpoints clearly indicate future service integrations.  
- The service must remain deployable and testable independently.  
- Database access is strictly limited to the catalog-service.  

These constraints ensure that the catalog-service is aligned with the overall decentralized LMS architecture.

---

## 4.7 Step 8 of ADD: Repeat Steps 2 through 7 for the Next Element of the System

With the catalog-service successfully migrated, tested, and documented, the system is ready for the next ADD iteration. Future iterations will focus on migrating services that depend on lending and reader behavior, building on the stable foundation established by the identity-service and catalog-service.
