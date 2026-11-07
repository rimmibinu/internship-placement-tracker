package com.campus.placement.dto;

import com.campus.placement.domain.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StatusUpdateRequest(
        @NotNull(message = "Status is required") ApplicationStatus status,
        @Size(max = 300, message = "Remarks must be at most 300 characters") String remarks) {}
