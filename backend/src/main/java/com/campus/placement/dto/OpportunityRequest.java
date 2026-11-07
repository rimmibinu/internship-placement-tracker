package com.campus.placement.dto;

import com.campus.placement.domain.OpportunityStatus;
import com.campus.placement.domain.OpportunityType;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record OpportunityRequest(
        @NotBlank(message = "Title is required") @Size(max = 150) String title,
        @NotNull(message = "Type is required") OpportunityType type,
        @NotNull(message = "Company is required") Long companyId,
        @Size(max = 2000, message = "Description must be at most 2000 characters") String description,
        @Size(max = 100) String location,
        @DecimalMin(value = "0.0", message = "Salary cannot be negative") Double salary,
        @NotNull(message = "Minimum CGPA is required") @DecimalMin(value = "0.0", message = "CGPA must be between 0 and 10")
        @DecimalMax(value = "10.0", message = "CGPA must be between 0 and 10") Double minCgpa,
        @NotNull(message = "Max backlogs is required") @Min(value = 0, message = "Backlogs cannot be negative") Integer maxBacklogs,
        @Size(max = 200) String allowedDepartments,
        @NotNull(message = "Openings is required") @Min(value = 1, message = "At least one opening is required") Integer openings,
        @NotNull(message = "Deadline is required") @FutureOrPresent(message = "Deadline cannot be in the past") LocalDate deadline,
        OpportunityStatus status) {}
