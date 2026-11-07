package com.campus.placement.service;

import com.campus.placement.dto.EligibilityResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** Same rules as the eligibility microservice; used only when the service is unreachable and fallback is enabled. */
public final class LocalEligibilityEvaluator {
    private LocalEligibilityEvaluator() {}

    public static EligibilityResult evaluate(double cgpa, int backlogs, String department,
                                             double minCgpa, int maxBacklogs, String allowedDepartments) {
        List<String> reasons = new ArrayList<>();
        if (cgpa < minCgpa) {
            reasons.add(String.format(Locale.ROOT, "CGPA %.2f is below the required %.2f", cgpa, minCgpa));
        }
        if (backlogs > maxBacklogs) {
            reasons.add("Active backlogs (" + backlogs + ") exceed the allowed " + maxBacklogs);
        }
        if (allowedDepartments != null && !allowedDepartments.isBlank()
                && !allowedDepartments.trim().equalsIgnoreCase("ALL")) {
            boolean ok = Arrays.stream(allowedDepartments.split(","))
                    .map(String::trim).anyMatch(d -> d.equalsIgnoreCase(department == null ? "" : department.trim()));
            if (!ok) reasons.add("Department " + department + " is not eligible (allowed: " + allowedDepartments + ")");
        }
        return new EligibilityResult(reasons.isEmpty(), reasons, "local-fallback");
    }
}
