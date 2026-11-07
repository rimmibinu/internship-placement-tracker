# Test plan

| ID | Level | Area | Test | Class |
|---|---|---|---|---|
| U1-U8 | Unit (Mockito) | Apply rules | success; deadline day OK; after deadline; closed role; duplicate; ineligible; one-offer policy; unknown student | `ApplicationServiceTest` |
| U9-U15 | Unit | Status rules | valid/invalid transition; admin cannot withdraw; openings full; offer OK; own-withdraw only; final state | `ApplicationServiceTest` |
| U16-18 | Unit | Fallback rules | pass; all reasons; "all" keyword | `LocalEligibilityEvaluatorTest` |
| M1-M7 | Unit | Microservice rules | threshold, backlogs, department, missing criteria | `EligibilityEvaluatorTest` |
| M8-M10 | API | Microservice contract | eligible, 3 reasons, validation 400 | `EligibilityControllerTest` |
| I1-I12 | Integration | Full path controller→service→JPA→H2 | 401; create entities; validation 400; duplicate 409; apply; ineligible 422; impersonation 403; role 403; transitions; search filter; dashboard + JDBC report; delete protection 409 | `PlacementApiIntegrationTest` |

## Manual UI checklist
1. Login as admin → dashboard numbers load. 2. Create company → opportunity (try past deadline: error shown). 3. Login as student → eligibility banner, apply, duplicate blocked.
4. Ineligible student sees reasons and a disabled Apply button. 5. Admin moves application through statuses; invalid moves are not offered. 6. Student withdraws.
7. Stop the eligibility service → still works with `local-fallback` (or 503 with fallback off). 8. Mobile width: hamburger menu, tables scroll.

Execution evidence: attach the `mvn test` console output / screenshots of the checklist to the report.
