package com.campus.placement.dto;

import com.campus.placement.domain.Opportunity;
import com.campus.placement.domain.OpportunityStatus;
import com.campus.placement.domain.OpportunityType;
import java.time.LocalDate;

public record OpportunityResponse(Long id, String title, OpportunityType type, Long companyId, String companyName,
                                  String description, String location, Double salary, double minCgpa, int maxBacklogs,
                                  String allowedDepartments, int openings, LocalDate deadline, OpportunityStatus status,
                                  boolean expired, long applicants, long offersMade) {
    public static OpportunityResponse from(Opportunity o, boolean expired, long applicants, long offersMade) {
        return new OpportunityResponse(o.getId(), o.getTitle(), o.getType(), o.getCompany().getId(), o.getCompany().getName(),
                o.getDescription(), o.getLocation(), o.getSalary(), o.getMinCgpa(), o.getMaxBacklogs(),
                o.getAllowedDepartments(), o.getOpenings(), o.getDeadline(), o.getStatus(), expired, applicants, offersMade);
    }
}
