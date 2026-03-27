# GEMINI.md - Auction Management System

This document provides context and guidelines for Gemini CLI interactions within the Auction Management System project.

## Project Overview

A robust RESTful API for an online auction system built with **Spring Boot 3.2.5** and **Java 17**. It handles user management, auction lifecycle, and concurrent bidding.

### Core Stack
- **Framework:** Spring Boot 3.2.5 (Web, Data JPA, Security, Validation)
- **Database:** PostgreSQL (Persistence)
- **Concurrency & Caching:** Redis (Distributed locking for bids, bid tracking via Sorted Sets)
- **Security:** Spring Security with OAuth2 Resource Server (JWT-based, stateless)
- **Build Tool:** Gradle

### Architectural Patterns
- **Standard Layering:** `Controller` -> `Service` -> `Repository`.
- **DTO Pattern:** Requests and responses use DTOs (e.g., `AuctionDTO`, `BidRequestDTO`).
- **Converters:** Specialized components (e.g., `AuctionToAuctionDTOConverter`) handle mapping between Entities and DTOs.
- **Unified Response:** All API responses are wrapped in a `me.vrishab.auction.system.Result` object for consistency.
- **Global Exception Handling:** Centralized logic in `ExceptionHandlerAdvice` to return consistent error responses.
- **Distributed Bidding:** Uses Redis distributed locks (`SETNX`) to ensure atomic bid updates and prevents race conditions.

## API Specification

- **Base URL:** `/api/v1`
- **Key Endpoints:**
  - `POST /api/v1/auth/login`: Authentication (Basic Auth -> JWT).
  - `GET /api/v1/auctions`: List auctions (supports pagination via `PageRequestParams`).
  - `POST /api/v1/auctions`: Create a new auction (Requires `ROLE_user`).
  - `PUT /api/v1/auctions/{id}/items/{itemId}/bid`: Place a bid on an item.

## Building and Running

### Prerequisites
- JDK 17
- Docker (for PostgreSQL and Redis)

### Infrastructure Setup
```bash
# Start PostgreSQL
docker run --name auction-db -e POSTGRES_DB=auction -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:latest

# Start Redis
docker run --name auction-redis -p 6379:6379 -d redis:7
```

### Commands
- **Build:** `./gradlew build`
- **Run:** `./gradlew bootRun`
- **Test:** `./gradlew test` (Uses Testcontainers for real DB/Redis integration tests)

## Development Conventions

- **Naming:** Follow standard Java/Spring camelCase conventions.
- **Testing:**
  - Use JUnit 5 and AssertJ.
  - Integration tests should use `@SpringBootTest` and Testcontainers where possible.
  - Unit tests for Services should mock Repositories using Mockito.
- **Lombok:** Use `@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, and `@Builder` to minimize boilerplate in DTOs and Entities.
- **Validation:** Use `jakarta.validation` annotations (e.g., `@NotBlank`, `@Min`) in DTOs.
- **Error Handling:** Throw specific exceptions (e.g., `AuctionNotFoundByIdException`) and let `ExceptionHandlerAdvice` handle the response.

## Key Files for Reference
- `src/main/resources/application.yml`: Configuration and API base URL.
- `src/main/java/me/vrishab/auction/auction/AuctionService.java`: Core bidding logic and Redis integration.
- `src/main/java/me/vrishab/auction/security/SecurityConfiguration.java`: Security filters and JWT setup.
- `src/main/java/me/vrishab/auction/system/Result.java`: Standard response wrapper.
