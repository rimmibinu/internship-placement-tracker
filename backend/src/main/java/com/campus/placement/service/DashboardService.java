package com.campus.placement.service;

import com.campus.placement.domain.ApplicationStatus;
import com.campus.placement.domain.OpportunityStatus;
import com.campus.placement.dto.DashboardResponse;
import com.campus.placement.dto.DashboardResponse.NameCount;
import com.campus.placement.repository.ApplicationRepository;
import com.campus.placement.repository.CompanyRepository;
import com.campus.placement.repository.OpportunityRepository;
import com.campus.placement.repository.StudentRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {
    private final StudentRepository students;
    private final CompanyRepository companies;
    private final OpportunityRepository opportunities;
    private final ApplicationRepository applications;
    private final OpportunityService opportunityService;
    private final Clock clock;

    public DashboardService(StudentRepository students, CompanyRepository companies, OpportunityRepository opportunities,
                            ApplicationRepository applications, OpportunityService opportunityService, Clock clock) {
        this.students = students;
        this.companies = companies;
        this.opportunities = opportunities;
        this.applications = applications;
        this.opportunityService = opportunityService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public DashboardResponse summary() {
        LocalDate today = LocalDate.now(clock);
        long totalStudents = students.count();
        long placed = applications.countStudentsPlaced();

        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (ApplicationStatus s : ApplicationStatus.values()) byStatus.put(s.name(), 0L);
        for (Object[] row : applications.countGroupedByStatus()) {
            byStatus.put(((ApplicationStatus) row[0]).name(), (Long) row[1]);
        }
        List<NameCount> byCompany = applications.countGroupedByCompany().stream()
                .map(r -> new NameCount((String) r[0], (Long) r[1])).toList();

        var upcoming = opportunities.findByStatusAndDeadlineBetweenOrderByDeadline(OpportunityStatus.OPEN, today, today.plusDays(14))
                .stream().map(o -> opportunityService.get(o.getId())).toList();

        return new DashboardResponse(totalStudents, companies.count(),
                opportunities.countByStatusAndDeadlineGreaterThanEqual(OpportunityStatus.OPEN, today),
                applications.count(), applications.countByStatus(ApplicationStatus.OFFERED), placed,
                totalStudents == 0 ? 0 : Math.round(placed * 1000.0 / totalStudents) / 10.0,
                byStatus, byCompany, upcoming);
    }
}
