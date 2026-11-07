package com.campus.eligibility;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record EligibilityRequest(
        @NotNull(message = "cgpa is required") @DecimalMin(value = "0.0", message = "cgpa must be >= 0") Double cgpa,
        @NotNull(message = "backlogs is required") @Min(value = 0, message = "backlogs must be >= 0") Integer backlogs,
        String department,
        Double minCgpa,
        Integer maxBacklogs,
        String allowedDepartments) {}
