package com.campus.placement.service;

import com.campus.placement.domain.Opportunity;
import com.campus.placement.domain.Student;
import com.campus.placement.dto.EligibilityResult;
import com.campus.placement.exception.ApiException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/** Calls the separate eligibility-service (CO-5 service boundary). */
@Component
public class EligibilityClient {
    private static final Logger log = LoggerFactory.getLogger(EligibilityClient.class);

    private final RestTemplate rest;
    private final String baseUrl;
    private final boolean fallbackEnabled;

    public EligibilityClient(RestTemplateBuilder builder,
                             @Value("${eligibility.url}") String baseUrl,
                             @Value("${eligibility.fallback-enabled:true}") boolean fallbackEnabled) {
        this.rest = builder.setConnectTimeout(Duration.ofSeconds(2)).setReadTimeout(Duration.ofSeconds(3)).build();
        this.baseUrl = baseUrl;
        this.fallbackEnabled = fallbackEnabled;
    }

    public EligibilityResult check(Student s, Opportunity o) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("cgpa", s.getCgpa());
        body.put("backlogs", s.getBacklogs());
        body.put("department", s.getDepartment());
        body.put("minCgpa", o.getMinCgpa());
        body.put("maxBacklogs", o.getMaxBacklogs());
        body.put("allowedDepartments", o.getAllowedDepartments());
        try {
            EligibilityResult r = rest.postForObject(baseUrl + "/api/eligibility/check", body, EligibilityResult.class);
            if (r == null) throw new RestClientException("Empty response");
            return r;
        } catch (RestClientException e) {
            if (fallbackEnabled) {
                log.warn("Eligibility service unreachable ({}); using local fallback", e.getMessage());
                return LocalEligibilityEvaluator.evaluate(s.getCgpa(), s.getBacklogs(), s.getDepartment(),
                        o.getMinCgpa(), o.getMaxBacklogs(), o.getAllowedDepartments());
            }
            throw ApiException.unavailable("Eligibility service is unavailable. Please try again later.");
        }
    }
}
