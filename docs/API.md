# API reference (main app, base `http://localhost:8080`)

All endpoints except `/api/auth/*` need header `X-User-Email: <email>`. Errors: `{ "status": 422, "message": "...", "errors": { "field": "msg" } }`.
Codes: 400 validation · 401 not logged in · 403 wrong role · 404 not found · 409 conflict · 422 business-rule violation · 503 eligibility service down (fallback off).

| Method & path | Role | Purpose |
|---|---|---|
| POST `/api/auth/login` `{email}` | public | Returns `{role,name,email,studentId}` |
| GET `/api/auth/demo-users` | public (demo mode) | Demo accounts |
| GET/POST `/api/companies`, GET/PUT/DELETE `/api/companies/{id}` | read: all · write: admin | Company CRUD, `?q=` search |
| GET/POST `/api/students`, GET/PUT/DELETE `/api/students/{id}` | admin (student: own GET) | Student CRUD, `?q=&department=` |
| GET `/api/opportunities?q=&type=&status=&companyId=&openOnly=` | all | Search/filter |
| POST/PUT/DELETE `/api/opportunities[/{id}]` | admin | Opportunity CRUD |
| PUT `/api/opportunities/{id}/status?value=OPEN\|CLOSED` | admin | Open/close |
| GET `/api/opportunities/{id}/eligibility[?studentId=]` | all | Eligibility check via microservice |
| GET `/api/applications?studentId=&opportunityId=&status=` | all (students see own only) | List (join of student/opportunity/company) |
| POST `/api/applications` | student (own) / admin | Apply |
| PUT `/api/applications/{id}/status` | admin | Move status |
| PUT `/api/applications/{id}/withdraw` | student (own) | Withdraw |
| GET `/api/dashboard` | admin | Totals, rates, grouped counts, upcoming deadlines |
| GET `/api/dashboard/department-report` | admin | Plain-JDBC department report |

## Examples
```http
POST /api/applications
X-User-Email: diya.sharma@campus.demo
{ "studentId": 2, "opportunityId": 1, "coverNote": "Keen to join" }

201 { "id": 16, "studentName": "Diya Sharma", "opportunityTitle": "Software Engineer Intern",
      "status": "APPLIED", "allowedNext": ["SHORTLISTED","REJECTED"], ... }

422 { "status": 422, "message": "Not eligible: CGPA 6.80 is below the required 7.50", "errors": {} }
409 { "status": 409, "message": "You have already applied to this opportunity", "errors": {} }
```
```http
PUT /api/applications/16/status            (admin)
{ "status": "SHORTLISTED", "remarks": "Strong profile" }
200 { "id": 16, "status": "SHORTLISTED", "remarks": "Strong profile", "allowedNext": ["INTERVIEW","REJECTED"] }
```

# Eligibility microservice (`http://localhost:8081`)
`POST /api/eligibility/check`
```json
{ "cgpa": 6.0, "backlogs": 3, "department": "MECH", "minCgpa": 7.5, "maxBacklogs": 1, "allowedDepartments": "CSE,IT" }
```
→ `200 { "eligible": false, "reasons": ["CGPA 6.00 is below the required 7.50", "Active backlogs (3) exceed the allowed 1", "Department MECH is not eligible (allowed: CSE,IT)"], "checkedBy": "eligibility-service" }`
`GET /api/eligibility/health` → `{status:"UP"}`. Run: `cd eligibility-service && mvn spring-boot:run` (port via `PORT`).
