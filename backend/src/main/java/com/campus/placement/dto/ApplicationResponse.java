package com.campus.placement.dto;

import com.campus.placement.domain.Application;
import com.campus.placement.domain.ApplicationStatus;
import com.campus.placement.domain.OpportunityType;
import java.time.LocalDateTime;
import java.util.List;

public record ApplicationResponse(Long id, Long studentId, String studentName, String rollNo, String department, double cgpa,
                                  Long opportunityId, String opportunityTitle, String companyName, OpportunityType type,
                                  ApplicationStatus status, String coverNote, String remarks,
                                  LocalDateTime appliedAt, LocalDateTime updatedAt, List<ApplicationStatus> allowedNext) {
    public static ApplicationResponse from(Application a) {
        return new ApplicationResponse(a.getId(), a.getStudent().getId(), a.getStudent().getName(), a.getStudent().getRollNo(),
                a.getStudent().getDepartment(), a.getStudent().getCgpa(), a.getOpportunity().getId(),
                a.getOpportunity().getTitle(), a.getOpportunity().getCompany().getName(), a.getOpportunity().getType(),
                a.getStatus(), a.getCoverNote(), a.getRemarks(), a.getAppliedAt(), a.getUpdatedAt(),
                a.getStatus().allowedNext().stream().filter(s -> s != ApplicationStatus.WITHDRAWN).toList());
    }
}
