package com.campus.placement.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "opportunities")
public class Opportunity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 150)
    private String title;
    @Enumerated(EnumType.STRING)
    @Column(name = "opportunity_type", nullable = false, length = 20)
    private OpportunityType type;
    @ManyToOne(optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
    @Column(length = 2000)
    private String description;
    @Column(length = 100)
    private String location;
    /** Stipend per month (internship) or CTC in LPA (placement). */
    private Double salary;
    @Column(name = "min_cgpa", nullable = false)
    private double minCgpa;
    @Column(name = "max_backlogs", nullable = false)
    private int maxBacklogs;
    /** "ALL" or comma separated department codes, e.g. "CSE,IT". */
    @Column(name = "allowed_departments", nullable = false, length = 200)
    private String allowedDepartments = "ALL";
    @Column(nullable = false)
    private int openings = 1;
    @Column(nullable = false)
    private LocalDate deadline;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OpportunityStatus status = OpportunityStatus.OPEN;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public OpportunityType getType() { return type; }
    public void setType(OpportunityType type) { this.type = type; }
    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Double getSalary() { return salary; }
    public void setSalary(Double salary) { this.salary = salary; }
    public double getMinCgpa() { return minCgpa; }
    public void setMinCgpa(double minCgpa) { this.minCgpa = minCgpa; }
    public int getMaxBacklogs() { return maxBacklogs; }
    public void setMaxBacklogs(int maxBacklogs) { this.maxBacklogs = maxBacklogs; }
    public String getAllowedDepartments() { return allowedDepartments; }
    public void setAllowedDepartments(String allowedDepartments) { this.allowedDepartments = allowedDepartments; }
    public int getOpenings() { return openings; }
    public void setOpenings(int openings) { this.openings = openings; }
    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
    public OpportunityStatus getStatus() { return status; }
    public void setStatus(OpportunityStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
