package com.campus.eligibility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

/** Pure business rules - no state, no database. */
@Component
public class EligibilityEvaluator {

    public EligibilityResult evaluate(EligibilityRequest r) {
        double minCgpa = r.minCgpa() == null ? 0.0 : r.minCgpa();
        int maxBacklogs = r.maxBacklogs() == null ? Integer.MAX_VALUE : r.maxBacklogs();
        List<String> reasons = new ArrayList<>();

        if (r.cgpa() < minCgpa) {
            reasons.add(String.format(Locale.ROOT, "CGPA %.2f is below the required %.2f", r.cgpa(), minCgpa));
        }
        if (r.backlogs() > maxBacklogs) {
            reasons.add("Active backlogs (" + r.backlogs() + ") exceed the allowed " + maxBacklogs);
        }
        String allowed = r.allowedDepartments();
        if (allowed != null && !allowed.isBlank() && !allowed.trim().equalsIgnoreCase("ALL")) {
            String dept = r.department() == null ? "" : r.department().trim();
            boolean ok = Arrays.stream(allowed.split(",")).map(String::trim).anyMatch(d -> d.equalsIgnoreCase(dept));
            if (!ok) reasons.add("Department " + r.department() + " is not eligible (allowed: " + allowed + ")");
        }
        return new EligibilityResult(reasons.isEmpty(), reasons, "eligibility-service");
    }
}
