package com.campus.placement.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, unique = true, length = 120)
    private String email;
    @Column(name = "roll_no", nullable = false, unique = true, length = 30)
    private String rollNo;
    @Column(nullable = false, length = 20)
    private String department;
    @Column(nullable = false)
    private double cgpa;
    @Column(nullable = false)
    private int backlogs;
    @Column(name = "graduation_year", nullable = false)
    private int graduationYear;
    @Column(length = 300)
    private String skills;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRollNo() { return rollNo; }
    public void setRollNo(String rollNo) { this.rollNo = rollNo; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public double getCgpa() { return cgpa; }
    public void setCgpa(double cgpa) { this.cgpa = cgpa; }
    public int getBacklogs() { return backlogs; }
    public void setBacklogs(int backlogs) { this.backlogs = backlogs; }
    public int getGraduationYear() { return graduationYear; }
    public void setGraduationYear(int graduationYear) { this.graduationYear = graduationYear; }
    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
}
