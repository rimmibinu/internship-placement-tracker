package com.campus.placement.dto;

import jakarta.validation.constraints.*;

public record StudentRequest(
        @NotBlank(message = "Name is required") @Size(max = 100) String name,
        @NotBlank(message = "Email is required") @Email(message = "Enter a valid email") @Size(max = 120) String email,
        @NotBlank(message = "Roll number is required") @Size(max = 30) String rollNo,
        @NotBlank(message = "Department is required") @Size(max = 20) String department,
        @NotNull(message = "CGPA is required") @DecimalMin(value = "0.0", message = "CGPA must be between 0 and 10")
        @DecimalMax(value = "10.0", message = "CGPA must be between 0 and 10") Double cgpa,
        @NotNull(message = "Backlogs is required") @Min(value = 0, message = "Backlogs cannot be negative") Integer backlogs,
        @NotNull(message = "Graduation year is required") @Min(value = 2020, message = "Year must be 2020 or later")
        @Max(value = 2040, message = "Year must be 2040 or earlier") Integer graduationYear,
        @Size(max = 300, message = "Skills must be at most 300 characters") String skills) {}
