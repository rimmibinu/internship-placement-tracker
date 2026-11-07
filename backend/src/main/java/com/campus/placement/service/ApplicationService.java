package com.campus.placement.service;

import com.campus.placement.domain.*;
import com.campus.placement.dto.*;
import com.campus.placement.exception.ApiException;
import com.campus.placement.repository.ApplicationRepository;
import com.campus.placement.repository.OpportunityRepository;
import com.campus.placement.repository.StudentRepository;
import jakarta.persistence.criteria.Predicate;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Business layer: all application rules live here. */
@Service
public class ApplicationService {
    private final StudentRepository students;
    private final OpportunityRepository opportunities;
    private final ApplicationRepository applications;
    private final EligibilityClient eligibility;
    private final Clock clock;

    public ApplicationService(StudentRepository students, OpportunityRepository opportunities,
                              ApplicationRepository applications, EligibilityClient eligibility, Clock clock) {
        this.students = students;
        this.opportunities = opportunities;
        this.applications = applications;
        this.eligibility = eligibility;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> list(Long studentId, Long opportunityId, ApplicationStatus status) {
        Specification<Application> spec = (root, cq, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (studentId != null) p.add(cb.equal(root.get("student").get("id"), studentId));
            if (opportunityId != null) p.add(cb.equal(root.get("opportunity").get("id"), opportunityId));
            if (status != null) p.add(cb.equal(root.get("status"), status));
            return cb.and(p.toArray(new Predicate[0]));
        };
        return applications.findAll(spec, Sort.by(Sort.Direction.DESC, "appliedAt")).stream()
                .map(ApplicationResponse::from).toList();
    }

    @Transactional
    public ApplicationResponse apply(ApplicationRequest req) {
        Student student = students.findById(req.studentId())
                .orElseThrow(() -> ApiException.notFound("Student " + req.studentId() + " not found"));
        Opportunity opp = opportunities.findById(req.opportunityId())
                .orElseThrow(() -> ApiException.notFound("Opportunity " + req.opportunityId() + " not found"));

        if (opp.getStatus() != OpportunityStatus.OPEN) {
            throw ApiException.businessRule("This opportunity is closed");
        }
        if (LocalDate.now(clock).isAfter(opp.getDeadline())) {
            throw ApiException.businessRule("The application deadline (" + opp.getDeadline() + ") has passed");
        }
        if (applications.existsByStudentIdAndOpportunityId(student.getId(), opp.getId())) {
            throw ApiException.conflict("You have already applied to this opportunity");
        }
        if (opp.getType() == OpportunityType.PLACEMENT && applications.existsByStudentIdAndStatusAndOpportunity_Type(
                student.getId(), ApplicationStatus.OFFERED, OpportunityType.PLACEMENT)) {
            throw ApiException.businessRule("Student already holds a placement offer (one-offer policy)");
        }
        EligibilityResult result = eligibility.check(student, opp);
        if (!result.eligible()) {
            throw ApiException.businessRule("Not eligible: " + String.join("; ", result.reasons()));
        }

        Application a = new Application();
        a.setStudent(student);
        a.setOpportunity(opp);
        a.setStatus(ApplicationStatus.APPLIED);
        a.setCoverNote(req.coverNote());
        return ApplicationResponse.from(applications.save(a));
    }

    @Transactional
    public ApplicationResponse updateStatus(Long id, ApplicationStatus target, String remarks) {
        Application a = find(id);
        if (target == ApplicationStatus.WITHDRAWN) {
            throw ApiException.businessRule("Only the student can withdraw an application");
        }
        if (!a.getStatus().canMoveTo(target)) {
            throw ApiException.businessRule("Cannot move application from " + a.getStatus() + " to " + target);
        }
        if (target == ApplicationStatus.OFFERED) {
            Opportunity o = a.getOpportunity();
            if (applications.countByOpportunityIdAndStatus(o.getId(), ApplicationStatus.OFFERED) >= o.getOpenings()) {
                throw ApiException.businessRule("All " + o.getOpenings() + " opening(s) for this role are already filled");
            }
            if (o.getType() == OpportunityType.PLACEMENT && applications.existsByStudentIdAndStatusAndOpportunity_Type(
                    a.getStudent().getId(), ApplicationStatus.OFFERED, OpportunityType.PLACEMENT)) {
                throw ApiException.businessRule("Student already holds a placement offer (one-offer policy)");
            }
        }
        a.setStatus(target);
        a.setRemarks(remarks);
        a.setUpdatedAt(LocalDateTime.now(clock));
        return ApplicationResponse.from(a);
    }

    /** @param actingStudentId null when an admin withdraws on behalf of nobody; otherwise must own the application */
    @Transactional
    public ApplicationResponse withdraw(Long id, Long actingStudentId) {
        Application a = find(id);
        if (actingStudentId != null && !a.getStudent().getId().equals(actingStudentId)) {
            throw ApiException.forbidden("You can only withdraw your own applications");
        }
        if (!a.getStatus().canMoveTo(ApplicationStatus.WITHDRAWN)) {
            throw ApiException.businessRule("An application in status " + a.getStatus() + " cannot be withdrawn");
        }
        a.setStatus(ApplicationStatus.WITHDRAWN);
        a.setUpdatedAt(LocalDateTime.now(clock));
        return ApplicationResponse.from(a);
    }

    private Application find(Long id) {
        return applications.findById(id).orElseThrow(() -> ApiException.notFound("Application " + id + " not found"));
    }
}
