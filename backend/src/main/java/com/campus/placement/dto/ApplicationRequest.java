package com.campus.placement.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ApplicationRequest(
        @NotNull(message = "Student is required") Long studentId,
        @NotNull(message = "Opportunity is required") Long opportunityId,
        @Size(max = 500, message = "Cover note must be at most 500 characters") String coverNote) {}
