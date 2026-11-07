package com.campus.placement.service;

import com.campus.placement.domain.Student;
import com.campus.placement.dto.StudentRequest;
import com.campus.placement.dto.StudentResponse;
import com.campus.placement.exception.ApiException;
import com.campus.placement.repository.ApplicationRepository;
import com.campus.placement.repository.StudentRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudentService {
    private final StudentRepository students;
    private final ApplicationRepository applications;

    public StudentService(StudentRepository students, ApplicationRepository applications) {
        this.students = students;
        this.applications = applications;
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> list(String q, String department) {
        return students.search(q == null ? "" : q.trim(), department == null ? "" : department.trim())
                .stream().map(StudentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public StudentResponse get(Long id) { return StudentResponse.from(find(id)); }

    @Transactional
    public StudentResponse create(StudentRequest r) {
        students.findByEmailIgnoreCase(r.email().trim()).ifPresent(s -> { throw ApiException.conflict("Email is already registered"); });
        students.findByRollNoIgnoreCase(r.rollNo().trim()).ifPresent(s -> { throw ApiException.conflict("Roll number is already registered"); });
        Student s = new Student();
        apply(s, r);
        return StudentResponse.from(students.save(s));
    }

    @Transactional
    public StudentResponse update(Long id, StudentRequest r) {
        Student s = find(id);
        students.findByEmailIgnoreCase(r.email().trim()).filter(x -> !x.getId().equals(id))
                .ifPresent(x -> { throw ApiException.conflict("Email is already registered"); });
        students.findByRollNoIgnoreCase(r.rollNo().trim()).filter(x -> !x.getId().equals(id))
                .ifPresent(x -> { throw ApiException.conflict("Roll number is already registered"); });
        apply(s, r);
        return StudentResponse.from(s);
    }

    @Transactional
    public void delete(Long id) {
        Student s = find(id);
        if (applications.existsByStudentId(id)) {
            throw ApiException.conflict("Cannot delete a student who has applications");
        }
        students.delete(s);
    }

    private Student find(Long id) {
        return students.findById(id).orElseThrow(() -> ApiException.notFound("Student " + id + " not found"));
    }

    private void apply(Student s, StudentRequest r) {
        s.setName(r.name().trim());
        s.setEmail(r.email().trim().toLowerCase());
        s.setRollNo(r.rollNo().trim().toUpperCase());
        s.setDepartment(r.department().trim().toUpperCase());
        s.setCgpa(r.cgpa());
        s.setBacklogs(r.backlogs());
        s.setGraduationYear(r.graduationYear());
        s.setSkills(r.skills());
    }
}
