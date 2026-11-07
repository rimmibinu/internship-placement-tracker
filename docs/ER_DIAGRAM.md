# ER Diagram

```mermaid
erDiagram
  COMPANIES ||--o{ OPPORTUNITIES : posts
  STUDENTS  ||--o{ APPLICATIONS  : submits
  OPPORTUNITIES ||--o{ APPLICATIONS : receives
  COMPANIES { bigint id PK string name UK string industry string website string location string description }
  STUDENTS { bigint id PK string name string email UK string roll_no UK string department double cgpa int backlogs int graduation_year string skills }
  OPPORTUNITIES { bigint id PK string title string opportunity_type bigint company_id FK double salary double min_cgpa int max_backlogs string allowed_departments int openings date deadline string status datetime created_at }
  APPLICATIONS { bigint id PK bigint student_id FK bigint opportunity_id FK string status string cover_note string remarks datetime applied_at datetime updated_at }
```
Constraints: `UNIQUE(student_id, opportunity_id)` on applications; FK deletes are blocked by the service layer (409) when referenced.
Full DDL: `db/schema.sql`. Sample queries: `db/queries.sql`.
