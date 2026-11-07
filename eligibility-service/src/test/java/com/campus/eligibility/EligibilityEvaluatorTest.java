package com.campus.eligibility;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class EligibilityEvaluatorTest {
    private final EligibilityEvaluator evaluator = new EligibilityEvaluator();

    @Test
    void allDepartmentsAllowsAnyone() {
        var r = evaluator.evaluate(new EligibilityRequest(7.0, 0, "MECH", 6.0, 2, "ALL"));
        assertTrue(r.eligible());
        assertTrue(r.reasons().isEmpty());
    }

    @Test
    void cgpaExactlyAtThresholdIsEligible() {
        assertTrue(evaluator.evaluate(new EligibilityRequest(7.5, 0, "CSE", 7.5, 0, "CSE")).eligible());
    }

    @Test
    void lowCgpaIsRejected() {
        var r = evaluator.evaluate(new EligibilityRequest(7.49, 0, "CSE", 7.5, 0, "CSE"));
        assertFalse(r.eligible());
        assertEquals(1, r.reasons().size());
    }

    @Test
    void tooManyBacklogsIsRejected() {
        assertFalse(evaluator.evaluate(new EligibilityRequest(9.0, 2, "CSE", 7.0, 1, "ALL")).eligible());
    }

    @Test
    void departmentMatchIsCaseInsensitive() {
        assertTrue(evaluator.evaluate(new EligibilityRequest(9.0, 0, "cse", 7.0, 1, "CSE, IT")).eligible());
    }

    @Test
    void wrongDepartmentIsRejected() {
        assertFalse(evaluator.evaluate(new EligibilityRequest(9.0, 0, "MECH", 7.0, 1, "CSE,IT")).eligible());
    }

    @Test
    void missingCriteriaMeansNoRestriction() {
        assertTrue(evaluator.evaluate(new EligibilityRequest(1.0, 9, "X", null, null, null)).eligible());
    }
}
