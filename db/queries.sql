-- Representative SQL used / demonstrated in the project (Unit III)

-- 1. Join: applications with student, role and company
SELECT s.name, s.roll_no, o.title, c.name AS company, a.status
FROM applications a
JOIN students s      ON s.id = a.student_id
JOIN opportunities o ON o.id = a.opportunity_id
JOIN companies c     ON c.id = o.company_id
ORDER BY a.applied_at DESC;

-- 2. Group + join: applications per company
SELECT c.name, COUNT(a.id) AS applications
FROM companies c
LEFT JOIN opportunities o ON o.company_id = c.id
LEFT JOIN applications a  ON a.opportunity_id = o.id
GROUP BY c.name ORDER BY applications DESC;

-- 3. Sub-query: students who have no application yet
SELECT name, roll_no FROM students
WHERE id NOT IN (SELECT student_id FROM applications);

-- 4. Set query: students with an offer OR a pending interview
SELECT s.name FROM students s JOIN applications a ON a.student_id = s.id WHERE a.status = 'OFFERED'
UNION
SELECT s.name FROM students s JOIN applications a ON a.student_id = s.id WHERE a.status = 'INTERVIEW';

-- 5. Department-wise report (same query the JDBC DAO runs)
SELECT s.department, COUNT(DISTINCT s.id) AS students, COUNT(a.id) AS applications,
       COALESCE(SUM(CASE WHEN a.status = 'OFFERED' THEN 1 ELSE 0 END), 0) AS offers
FROM students s LEFT JOIN applications a ON a.student_id = s.id
GROUP BY s.department ORDER BY s.department;

-- 6. DML examples
UPDATE opportunities SET status = 'CLOSED' WHERE deadline < CURRENT_DATE;
DELETE FROM applications WHERE status = 'WITHDRAWN' AND updated_at < NOW() - INTERVAL 1 YEAR;
ALTER TABLE students ADD COLUMN placement_year INT NULL;
