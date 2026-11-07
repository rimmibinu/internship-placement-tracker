package com.campus.placement.web;

import com.campus.placement.config.AuthInterceptor;
import com.campus.placement.config.CurrentUser;
import com.campus.placement.domain.OpportunityStatus;
import com.campus.placement.domain.OpportunityType;
import com.campus.placement.dto.EligibilityResult;
import com.campus.placement.dto.OpportunityRequest;
import com.campus.placement.dto.OpportunityResponse;
import com.campus.placement.exception.ApiException;
import com.campus.placement.service.OpportunityService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/opportunities")
public class OpportunityController {
    private final OpportunityService service;

    public OpportunityController(OpportunityService service) { this.service = service; }

    @GetMapping
    public List<OpportunityResponse> list(@RequestParam(required = false) String q,
                                          @RequestParam(required = false) OpportunityType type,
                                          @RequestParam(required = false) OpportunityStatus status,
                                          @RequestParam(required = false) Long companyId,
                                          @RequestParam(defaultValue = "false") boolean openOnly) {
        return service.list(q, type, status, companyId, openOnly);
    }

    @GetMapping("/{id}")
    public OpportunityResponse get(@PathVariable Long id) { return service.get(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OpportunityResponse create(@Valid @RequestBody OpportunityRequest r) { return service.create(r); }

    @PutMapping("/{id}")
    public OpportunityResponse update(@PathVariable Long id, @Valid @RequestBody OpportunityRequest r) { return service.update(id, r); }

    @PutMapping("/{id}/status")
    public OpportunityResponse setStatus(@PathVariable Long id, @RequestParam OpportunityStatus value) {
        return service.setStatus(id, value);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }

    @GetMapping("/{id}/eligibility")
    public EligibilityResult eligibility(@RequestAttribute(AuthInterceptor.ATTR) CurrentUser user, @PathVariable Long id,
                                         @RequestParam(required = false) Long studentId) {
        Long sid = user.isStudent() ? user.studentId() : studentId;
        if (sid == null) throw ApiException.businessRule("studentId is required");
        return service.checkEligibility(id, sid);
    }
}
