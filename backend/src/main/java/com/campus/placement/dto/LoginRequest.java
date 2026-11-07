package com.campus.placement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank(message = "Email is required") @Email(message = "Enter a valid email") String email) {}
