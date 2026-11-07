package com.campus.eligibility;

import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/eligibility")
public class EligibilityController {
    private final EligibilityEvaluator evaluator;

    public EligibilityController(EligibilityEvaluator evaluator) { this.evaluator = evaluator; }

    @PostMapping("/check")
    public EligibilityResult check(@Valid @RequestBody EligibilityRequest request) {
        return evaluator.evaluate(request);
    }

    @GetMapping("/health")
    public Map<String, String> health() { return Map.of("status", "UP", "service", "eligibility-service"); }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> invalid(MethodArgumentNotValidException e) {
        Map<String, String> errors = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(f -> errors.putIfAbsent(f.getField(), f.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Map.of("message", "Validation failed", "errors", errors));
    }
}
