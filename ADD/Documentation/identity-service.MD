# ADD First Iteration – Identity Service

## 4. ADD First Iteration

## 4.1 Step 1 of ADD: Confirm There Is Sufficient Requirements Information

At the start of the first ADD iteration, sufficient requirements information was available to proceed with architectural design.  
The system requirements clearly stated the need for user registration, authentication, authorization, and role-based access control. These concerns are foundational and must be solved before other domain services can be safely migrated.  
The identity-related requirements were well-defined, stable, and largely independent of other business functionality. This made the Identity Service a suitable candidate for the first architectural iteration.

---

## 4.2 Step 2 of ADD: Choose an Element of the System to Decompose

For the first iteration, the chosen element to decompose was authentication and user management, which was extracted into a dedicated Identity Service.  
The Identity Service was selected first because:

- All other services depend on authentication and authorization.  
- Identity logic is cross-cutting and shared across the system.  
- Decoupling identity concerns early reduces risk in later migrations.  

---

## 4.3 Step 3 of ADD: Identify Candidate Architectural Drivers

The main architectural drivers for the Identity Service are:

### Functional Drivers

- Users must be able to register and log in.  
- The system must support role-based access control.  
- Librarians must be able to manage users.  

### Quality Drivers

- Security: Safe authentication, password handling, and authorization.  
- Reliability: Predictable behavior after restarts and clean database setups.  
- Deployability: The service must run end-to-end using Docker.  
- Maintainability: Clear responsibilities and stable APIs.  

These drivers guided all design and implementation decisions in this iteration.

---

## 4.4 Step 4 of ADD: Choose a Design Concept that Satisfies the Architectural Drivers

The Identity Service was designed as a standalone Spring Boot microservice responsible for authentication, authorization, and user management.  
Key design concepts include:

- Stateless authentication using JWT  
- Centralized identity management  
- REST-based APIs  
- Database per service  
- Clear separation from domain logic  

This design satisfies both security and scalability requirements while keeping the service easy to maintain.

---

## 4.4.1 Step 4, Substep 1 of ADD: Identify Design Concerns

The main design concerns identified were:

- Secure password storage  
- Stateless authentication  
- Role-based access control  
- Clear separation between public and admin endpoints  
- Safe user deletion without breaking references  
- Clean startup with an empty database  

---

## 4.4.2 Step 4, Substep 2 of ADD: List Alternative Patterns for Subordinate Concerns

The following alternatives were considered:

- Session-based authentication vs JWT  
- Hard delete vs soft delete for users  
- Admin-only user management vs librarian access  
- Shared authentication logic vs dedicated identity service  

---

## 4.4.3 Step 4, Substep 3 of ADD: Select Patterns from the List

The following patterns were selected:

- JWT-based stateless authentication  
- Soft delete for users  
- Dedicated Identity Service  
- Role-based access control via URL patterns  
- Docker-based deployment  

These patterns were chosen to balance security, simplicity, and robustness.

---

## 4.4.4 Step 4, Substep 4 of ADD: Determine Relationship Between Patterns and Drivers

- JWT supports scalability and statelessness.  
- Soft delete improves reliability and future extensibility.  
- URL-based access control simplifies authorization logic.  
- A dedicated service improves modularity and maintainability.  

Each pattern directly supports one or more architectural drivers.

---

## 4.4.5 Step 4, Substep 5 of ADD: Capture Preliminary Architectural Views

At this stage, the Identity Service consists of:

- Public authentication endpoints  
- Admin user management endpoints  
- JWT security configuration  
- A dedicated MySQL database  
- Docker-based deployment configuration  

This represents the stable architectural structure of the service.

---

## 4.4.6 Step 4.6 of ADD: Evaluate and Resolve Inconsistencies

During development and testing, several issues were identified and resolved:

- JWT token handling was verified across restarts.  
- Database schema creation was validated using clean Docker setups.  
- Role-based access rules were refined.  
- Error handling and HTTP status codes were made consistent.  

---

## 4.5 Step 5 of ADD: Instantiate Architectural Elements and Allocate Responsibilities

Responsibilities were allocated as follows:

- Identity Service: Authentication, authorization, and user management  
- Public API: Registration and login  
- Admin API: User CRUD operations and search  
- Database (identity_db): User and role persistence  
- Security Layer: JWT validation and access control  

---

## 4.6 Step 7 of ADD: Verify and Refine Requirements and Make Them Constraints

After implementation and testing, the following constraints were confirmed:

- All protected endpoints require a valid JWT.  
- Passwords must be stored using BCrypt hashing.  
- Users are never physically deleted.  
- The service must start correctly with an empty database.  
- The Identity Service must remain independent of other domain services.  

---

## 4.7 Step 8 of ADD: Repeat Steps 2 through 7 for the Next Element of the System

With the Identity Service complete and stable, the system was ready for the next ADD iteration.  
This led directly to the migration of the catalog-service in the second iteration.
