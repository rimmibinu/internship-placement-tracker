package com.campus.placement.dto;

import java.util.List;
import java.util.Map;

public record DashboardResponse(long totalStudents, long totalCompanies, long openOpportunities, long totalApplications,
                                long offers, long studentsPlaced, double placementRate,
                                Map<String, Long> applicationsByStatus, List<NameCount> applicationsByCompany,
                                List<OpportunityResponse> upcomingDeadlines) {
    public record NameCount(String name, long count) {}
}
