# Architecture

```mermaid
flowchart LR
  U[Browser: React SPA] -- "REST /api (JSON, X-User-Email)" --> B
  subgraph B[Main app :8080 - Spring Boot]
    C[Presentation: controllers + AuthInterceptor + GlobalExceptionHandler]
    S[Business: ApplicationService, OpportunityService, ...]
    R[Data: Spring Data JPA repositories + JdbcReportDao]
    C --> S --> R
  end
  S -- "POST /api/eligibility/check" --> E[Eligibility microservice :8081]
  R --> D[(H2 file DB / MySQL)]
```

## Three-layer separation (CO-4)
| Layer | Package | Responsibility |
|---|---|---|
| Presentation | `web`, `config` | HTTP mapping, validation (`@Valid`), role checks, error JSON |
| Business | `service` | Rules: deadline, duplicates, eligibility, offer policy, transitions, openings |
| Domain / data | `domain`, `repository` | JPA entities, repositories, JDBC report |

## Service boundary (CO-5)
`eligibility-service` is a separate Spring Boot process with its own pom, port, tests and Dockerfile. It owns the eligibility rules
(CGPA, backlogs, department) and is stateless. The main app calls it via `EligibilityClient` (RestTemplate, 2 s connect / 3 s read timeouts).
If it is unreachable, `LocalEligibilityEvaluator` provides identical rules when `eligibility.fallback-enabled=true` (resilience pattern).

## Application lifecycle
`APPLIED → SHORTLISTED → INTERVIEW → OFFERED` ; any non-final state → `REJECTED` (officer) or `WITHDRAWN` (student). OFFERED/REJECTED/WITHDRAWN are final.
