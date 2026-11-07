package com.campus.placement;

import static org.junit.jupiter.api.Assertions.*;

import com.campus.placement.service.LocalEligibilityEvaluator;
import org.junit.jupiter.api.Test;

class LocalEligibilityEvaluatorTest {
    @Test
    void passesWhenAllCriteriaMet() {
        assertTrue(LocalEligibilityEvaluator.evaluate(8.0, 0, "CSE", 7.0, 1, "CSE,IT").eligible());
    }

    @Test
    void collectsEveryFailureReason() {
        var r = LocalEligibilityEvaluator.evaluate(5.0, 4, "MECH", 7.0, 1, "CSE");
        assertFalse(r.eligible());
        assertEquals(3, r.reasons().size());
        assertEquals("local-fallback", r.checkedBy());
    }

    @Test
    void allKeywordIsCaseInsensitive() {
        assertTrue(LocalEligibilityEvaluator.evaluate(8.0, 0, "MECH", 7.0, 1, "all").eligible());
    }
}
