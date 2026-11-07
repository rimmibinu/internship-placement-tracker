package com.campus.placement.dto;

import java.util.List;

/** Result returned by the eligibility microservice (or by the local fallback). */
public record EligibilityResult(boolean eligible, List<String> reasons, String checkedBy) {}
