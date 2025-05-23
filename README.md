## Rate-Limited API Gateway Middleware

A Spring Boot based middleware that enforces **rate limiting** per authenticated user using **Redis** and **JWT-based authentication**.

---

## Problem Statement

Implement a rate-limiting gateway middleware that:

- Allows **100 requests per minute** per authenticated user.
- Uses **Redis** to store and increment counters.
- Returns **HTTP 429 (Too Many Requests)** if the user exceeds the limit.
- Integrates with **Spring Security** for JWT-based authentication.

---

## Design Decisions

## Authentication
- JWT-based authentication is assumed.
- A valid JWT token is required in the `Authorization` header for each request.
- Username is extracted from the token and used to identify the user in rate-limiting logic.

## Rate Limiting Strategy
- Implements the **Fixed Window** rate-limiting algorithm.
- Each user gets a unique Redis key per time window (e.g., `rate_limit:{username}:{epoch_minute}`).
- Uses Redis' atomic `INCR` and `EXPIRE` commands for thread-safe and performant tracking.
- TTL ensures automatic reset of counters after 60 seconds.

## Redis
- Redis is used as the in-memory data store to maintain counters for each user.
- Redis TTL helps to keep the store clean and avoid stale data buildup.

## Concurrency & Performance
- Redis operations are atomic to handle high concurrency.
- Rate-limiting logic is implemented in a Spring `OncePerRequestFilter` for optimal performance.

---

## Tech Stack

| Component       | Technology             |
|-----------------|------------------------|
| Language        | Java 17                |
| Framework       | Spring Boot 3.x        |
| Authentication  | Spring Security, JWT   |
| Rate Limiting   | Redis                  |
| Testing         | JUnit 5, Mockito       |
| Build Tool      | Gradle                 |

---

## Testing Strategy

- Unit tests written for the rate-limiting service.
- Uses **Mockito** to mock Redis operations.
- Test coverage includes:
  - Under and over-limit request scenarios
  - Window reset behavior
  - Concurrent access simulation

---

## Run application using

./gradlew bootRun

---

## Include your jwt token in the header

Authorization: Bearer <your_jwt_token>

---

## Run tests using:

./gradlew test

---

## Architecture Diagram:

![Architecture Diagram](https://github.com/user-attachments/assets/0a8564ce-f597-4831-980e-5b915396f49b)

---

## Assumptions:

- JWT tokens are valid and already contain the username as a claim.
- The same rate limit (100 requests/minute) applies to all authenticated users.
- All protected endpoints go through the rate-limiting filter.

---

## Test Reports can be found out at: 

https://github.com/kunal71295/rate-limiter/blob/main/build/reports/tests/test/index.html
