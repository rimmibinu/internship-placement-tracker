package com.campus.placement.service;

import com.campus.placement.domain.*;
import com.campus.placement.dto.EligibilityResult;
import com.campus.placement.dto.OpportunityRequest;
import com.campus.placement.dto.OpportunityResponse;
import com.campus.placement.exception.ApiException;
import com.campus.placement.repository.ApplicationRepository;
import com.campus.placement.repository.CompanyRepository;
import com.campus.placement.repository.OpportunityRepository;
import com.campus.placement.repository.StudentRepository;
import jakarta.persistence.criteria.Predicate;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OpportunityService {
    private final OpportunityRepository opportunities;
    private final CompanyRepository companies;
    private final ApplicationRepository applications;
    private final StudentRepository students;
    private final EligibilityClient eligibility;
    private final Clock clock;

    public OpportunityService(OpportunityRepository opportunities, CompanyRepository companies,
                              ApplicationRepository applications, StudentRepository students,
                              EligibilityClient eligibility, Clock clock) {
        this.opportunities = opportunities;
        this.companies = companies;
        this.applications = applications;
        this.students = students;
        this.eligibility = eligibility;
        this.clock = clock;
    }

    /** Search/filter: free text, type, status, company, "open only" (status OPEN and deadline not passed). */
    @Transactional(readOnly = true)
    public List<OpportunityResponse> list(String q, OpportunityType type, OpportunityStatus status,
                                          Long companyId, boolean openOnly) {
        LocalDate today = LocalDate.now(clock);
        Specification<Opportunity> spec = (root, cq, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase() + "%";
                p.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("company").get("name")), like),
                        cb.like(cb.lower(cb.coalesce(root.<String>get("location"), "")), like)));
            }
            if (type != null) p.add(cb.equal(root.get("type"), type));
            if (status != null) p.add(cb.equal(root.get("status"), status));
            if (companyId != null) p.add(cb.equal(root.get("company").get("id"), companyId));
            if (openOnly) {
                p.add(cb.equal(root.get("status"), OpportunityStatus.OPEN));
                p.add(cb.greaterThanOrEqualTo(root.<LocalDate>get("deadline"), today));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
        return opportunities.findAll(spec, Sort.by(Sort.Direction.ASC, "deadline")).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public OpportunityResponse get(Long id) { return toResponse(find(id)); }

    @Transactional
    public OpportunityResponse create(OpportunityRequest r) {
        Opportunity o = new Opportunity();
        apply(o, r);
        return toResponse(opportunities.save(o));
    }

    @Transactional
    public OpportunityResponse update(Long id, OpportunityRequest r) {
        Opportunity o = find(id);
        apply(o, r);
        return toResponse(o);
    }

    @Transactional
    public OpportunityResponse setStatus(Long id, OpportunityStatus status) {
        Opportunity o = find(id);
        o.setStatus(status);
        return toResponse(o);
    }

    @Transactional
    public void delete(Long id) {
        Opportunity o = find(id);
        if (applications.existsByOpportunityId(id)) {
            throw ApiException.conflict("Cannot delete an opportunity that has applications. Close it instead.");
        }
        opportunities.delete(o);
    }

    /** Checks one student against one opportunity through the eligibility service. */
    @Transactional(readOnly = true)
    public EligibilityResult checkEligibility(Long opportunityId, Long studentId) {
        Opportunity o = find(opportunityId);
        Student s = students.findById(studentId).orElseThrow(() -> ApiException.notFound("Student " + studentId + " not found"));
        return eligibility.check(s, o);
    }

    private Opportunity find(Long id) {
        return opportunities.findById(id).orElseThrow(() -> ApiException.notFound("Opportunity " + id + " not found"));
    }

    private void apply(Opportunity o, OpportunityRequest r) {
        Company c = companies.findById(r.companyId()).orElseThrow(() -> ApiException.notFound("Company " + r.companyId() + " not found"));
        o.setTitle(r.title().trim());
        o.setType(r.type());
        o.setCompany(c);
        o.setDescription(r.description());
        o.setLocation(r.location());
        o.setSalary(r.salary());
        o.setMinCgpa(r.minCgpa());
        o.setMaxBacklogs(r.maxBacklogs());
        o.setAllowedDepartments(normalizeDepartments(r.allowedDepartments()));
        o.setOpenings(r.openings());
        o.setDeadline(r.deadline());
        if (r.status() != null) o.setStatus(r.status());
    }

    static String normalizeDepartments(String raw) {
        if (raw == null || raw.isBlank() || raw.trim().equalsIgnoreCase("ALL")) return "ALL";
        return String.join(",", java.util.Arrays.stream(raw.split(",")).map(String::trim)
                .filter(s -> !s.isEmpty()).map(String::toUpperCase).distinct().toList());
    }

    private OpportunityResponse toResponse(Opportunity o) {
        boolean expired = LocalDate.now(clock).isAfter(o.getDeadline());
        return OpportunityResponse.from(o, expired, applications.countByOpportunityId(o.getId()),
                applications.countByOpportunityIdAndStatus(o.getId(), ApplicationStatus.OFFERED));
    }
}
