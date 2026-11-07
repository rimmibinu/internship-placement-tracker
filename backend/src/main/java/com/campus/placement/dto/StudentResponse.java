package com.campus.placement.dto;

import com.campus.placement.domain.Student;

public record StudentResponse(Long id, String name, String email, String rollNo, String department,
                              double cgpa, int backlogs, int graduationYear, String skills) {
    public static StudentResponse from(Student s) {
        return new StudentResponse(s.getId(), s.getName(), s.getEmail(), s.getRollNo(), s.getDepartment(),
                s.getCgpa(), s.getBacklogs(), s.getGraduationYear(), s.getSkills());
    }
}
