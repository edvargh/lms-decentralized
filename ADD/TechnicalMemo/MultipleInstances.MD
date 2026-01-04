# Technical Memo 03 – Horizontal Scaling with Multiple Service Instances

## Issue

The system must support increased load and concurrent users without relying on a single instance per service, which would create bottlenecks and single points of failure.

---

## Problem

How to run multiple instances of each backend service while maintaining correct routing, security, and service discovery, without introducing complex orchestration or client-side awareness of scaling.

---

## Summary of Solution

Deploy multiple instances per service using Docker Compose scaling, and introduce an Nginx API Gateway configured with dynamic upstream resolution to load-balance traffic across service instances.

---

## Factors

The system is already decomposed into independent Spring Boot services with stateless REST APIs and JWT-based authentication. Docker Compose is used for local orchestration, and all services expose identical ports internally.

---

## Solution

Each service (identity, catalog, reader-lending) is configured to run multiple instances using Docker Compose scaling.  
An Nginx gateway acts as a single entry point and routes incoming requests to service-specific upstreams.  
Nginx uses Docker’s internal DNS resolver to dynamically resolve service instances and distribute requests across them.  
Because authentication is stateless and handled via JWTs, no session affinity or shared state is required between instances.

---

## Motivation

This solution improves scalability, resilience, and availability while keeping the architecture simple and transparent to clients.  
It aligns with cloud-native principles and allows horizontal scaling without introducing a full container orchestration platform.  
The approach supports gradual migration toward more advanced platforms (e.g., Kubernetes) if needed later.

---

## Alternatives

- Run a single instance per service and scale vertically.  
- Implement client-side load balancing.  
- Introduce a full orchestration platform such as Kubernetes prematurely.

---

## Justification

Multiple service instances are supported through stateless service design and gateway-level load balancing. All instances are interchangeable and connect to the same persistent data store, allowing requests to be distributed without coordination or shared state. This approach enables horizontal scaling, improves fault tolerance, and requires no changes to service implementation when additional instances are introduced.

---

## Pending Issues

- How to handle centralized logging and monitoring across multiple instances.  
- Whether rate limiting should be applied at the gateway or service level.
