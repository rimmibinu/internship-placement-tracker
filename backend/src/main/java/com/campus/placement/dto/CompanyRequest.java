package com.campus.placement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyRequest(
        @NotBlank(message = "Company name is required") @Size(max = 120, message = "Name must be at most 120 characters") String name,
        @Size(max = 80, message = "Industry must be at most 80 characters") String industry,
        @Size(max = 200, message = "Website must be at most 200 characters") String website,
        @Size(max = 100, message = "Location must be at most 100 characters") String location,
        @Size(max = 1000, message = "Description must be at most 1000 characters") String description) {}
