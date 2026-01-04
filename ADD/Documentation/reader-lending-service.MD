# ADD Third Iteration – Migration of Reader-Lending-Service

## 4. ADD Third Iteration

## 4.1 Step 1 of ADD: Confirm There Is Sufficient Requirements Information

At the start of the third ADD iteration, sufficient requirements information was available to proceed.  
After migrating identity-service and catalog-service, the next clear requirement was to move lending and reader functionality into its own service.  
The requirements for lendings (overdue, return, averages) and reader data (search, top readers, photos) were clear enough to migrate the parts that do not require live communication with other services.

---

## 4.2 Step 2 of ADD: Choose an Element of the System to Decompose

For the third iteration, the chosen element to decompose was the reader-lending-service (ReaderManagement + LendingManagement).  
This service was selected because:

- It contains core LMS business logic (readers + lendings).  
- It can be made mostly independent using a database-per-service approach.  
- Some endpoints depend on other services, so they can be stubbed temporarily while keeping the rest working.  

---

## 4.3 Step 3 of ADD: Identify Candidate Architectural Drivers

The main architectural drivers for the reader-lending-service are:

### Functional Drivers

- Support lending operations such as getting a lending, setting it as returned, listing overdue lendings, and computing average duration.  
- Support reader operations such as listing readers (staff), searching readers, showing top readers, and handling reader photos.  
- Enforce correct access rules: staff can see more, readers can only see their own data.  

### Quality Drivers

- Modularity: lending/reader logic must be isolated in this service.  
- Security: endpoints must be protected using JWT tokens from identity-service.  
- Data Ownership: the service must own its database and not read other services’ tables.  
- Maintainability: clear separation between implemented endpoints and stubbed ones.  
- Reliability: should start cleanly after wiping databases and restarting Docker.  

---

## 4.4 Step 4 of ADD: Choose a Design Concept that Satisfies the Architectural Drivers

The reader-lending-service was designed as an independent Spring Boot microservice with:

- REST APIs for reader and lending functionality  
- Database per service (local persistence for readers, lendings, fines, photos metadata)  
- JWT-based security using tokens issued by identity-service  
- Reduced coupling to catalog-service by removing Book from Lending and using ISBN instead  
- Temporary stubs for endpoints that depend on missing cross-service integration, with a plan to reintroduce them later  

---

## 4.4.1 Step 4, Substep 1 of ADD: Identify Design Concerns

The main design concerns identified were:

- Removing the dependency on catalog-service inside lending (Book → ISBN).  
- Handling migration gaps safely (don’t break; return clear “Not Implemented” where needed).  
- Enforcing staff vs reader access rules correctly.  
- Handling optimistic locking for updates using ETag/If-Match.  
- Testing behavior after wiping databases (docker compose down -v) and starting fresh.  

---

## 4.4.2 Step 4, Substep 2 of ADD: List Alternative Patterns for Subordinate Concerns

The following alternatives were considered:

- Keep Book as a reference vs store only ISBN in Lending.  
- Implement cross-service calls now vs stub dependent endpoints until integration is ready.  
- Allow missing flows to fail at runtime vs disable them explicitly with clear error responses.  
- Shared database vs database per service.  

---

## 4.4.3 Step 4, Substep 3 of ADD: Select Patterns from the List

The following patterns were selected:

- Database per service (reader-lending-service owns its schema).  
- JWT authentication delegated to identity-service.  
- REST APIs for the implemented operations.  
- Optimistic locking with ETag and If-Match for PATCH endpoints.  
- ISBN stored in Lending instead of a Book relationship.  
- Stub endpoints that depend on other services, returning HTTP 501 with clear messages.  

---

## 4.4.4 Step 4, Substep 4 of ADD: Determine Relationship Between Patterns and Drivers

- ISBN instead of Book improves Modularity and Data Ownership.  
- JWT-based security supports Security and keeps services stateless.  
- Stubbing dependent endpoints improves Reliability and Maintainability during migration.  
- If-Match + versioning supports Reliability by preventing lost updates.  

---

## 4.4.5 Step 4, Substep 5 of ADD: Capture Preliminary Architectural Views

At this stage, the reader-lending-service consists of:

- ReaderController with endpoints for reader data, search, top readers, and photos  
- LendingController with endpoints for getting a lending, setting returned, overdue, and average duration  
- ReaderServiceImpl and LendingServiceImpl containing business logic  
- Repositories for ReaderDetails, Lending, Fine, and Photo metadata  
- JWT security configuration validating tokens from identity-service  
- Stubbed endpoints for flows that require catalog-service or deeper identity-service integration (createReader, createLending, and selected search features)  

---

## 4.4.6 Step 4.6 of ADD: Evaluate and Resolve Inconsistencies

During endpoint testing from a clean Docker setup, the following points were confirmed:

- Register/login happens in identity-service and tokens are accepted by reader-lending-service.  
- Some flows cannot work after a DB wipe because reader creation is stubbed, meaning a logged-in READER may not exist in the reader-lending database yet (resulting in a controlled 404 for ReaderDetails).  
- Stubbed endpoints return 501 Not Implemented with clear messages instead of failing unpredictably.  
- Access control is enforced (staff vs reader), and conditional PATCH requires If-Match.  

---

## 4.5 Step 5 of ADD: Instantiate Architectural Elements and Allocate Responsibilities

Responsibilities were allocated as follows:

- Identity-Service: register/login + JWT issuance  
- Reader-Lending-Service: owns reader + lending data and logic (including fines and reader photos)  
- ReaderController/LendingController: REST endpoints and access rules  
- Service layer: business logic, validation, and persistence coordination  
- Database (reader-lending DB): stores ReaderDetails, Lendings, Fines, and Photo metadata  

---

## 4.6 Step 7 of ADD: Verify and Refine Requirements and Make Them Constraints

After migration and testing, the following constraints were confirmed:

- Protected endpoints require a valid JWT from identity-service.  
- Lending must not depend on catalog-service entities; Lending stores ISBN instead.  
- Endpoints depending on other services are explicitly stubbed and documented as temporary.  
- Readers must only access their own data; staff can access wider data.  
- PATCH updates must use If-Match for concurrency safety.  

---

## 4.7 Step 8 of ADD: Repeat Steps 2 through 7 for the Next Element of the System

With reader-lending-service migrated into an independent service and stabilized for the parts that do not require cross-service data, the next iteration can focus on reintroducing the stubbed flows through proper service integration.  
This includes reintroducing createReader and createLending using cross-service communication with identity-service and catalog-service, while keeping database ownership and boundaries intact.
