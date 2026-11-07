package com.campus.placement.web;

import com.campus.placement.dto.DashboardResponse;
import com.campus.placement.repository.JdbcReportDao;
import com.campus.placement.service.DashboardService;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService service;
    private final JdbcReportDao jdbcReport;

    public DashboardController(DashboardService service, JdbcReportDao jdbcReport) {
        this.service = service;
        this.jdbcReport = jdbcReport;
    }

    @GetMapping
    public DashboardResponse summary() { return service.summary(); }

    /** Produced with plain JDBC (DriverManager/Statement/ResultSet). */
    @GetMapping("/department-report")
    public List<Map<String, Object>> departmentReport() { return jdbcReport.departmentReport(); }
}
