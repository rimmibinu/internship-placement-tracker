package com.campus.eligibility;

import java.util.List;

public record EligibilityResult(boolean eligible, List<String> reasons, String checkedBy) {}
