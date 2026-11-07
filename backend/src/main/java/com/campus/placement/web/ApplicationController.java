package com.campus.placement.web;

import com.campus.placement.config.AuthInterceptor;
import com.campus.placement.config.CurrentUser;
import com.campus.placement.domain.ApplicationStatus;
import com.campus.placement.dto.ApplicationRequest;
import com.campus.placement.dto.ApplicationResponse;
import com.campus.placement.dto.StatusUpdateRequest;
import com.campus.placement.exception.ApiException;
import com.campus.placement.service.ApplicationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {
    private final ApplicationService service;

    public ApplicationController(ApplicationService service) { this.service = service; }

    @GetMapping
    public List<ApplicationResponse> list(@RequestAttribute(AuthInterceptor.ATTR) CurrentUser user,
                                          @RequestParam(required = false) Long studentId,
                                          @RequestParam(required = false) Long opportunityId,
                                          @RequestParam(required = false) ApplicationStatus status) {
        Long sid = user.isStudent() ? user.studentId() : studentId;
        return service.list(sid, opportunityId, status);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse apply(@RequestAttribute(AuthInterceptor.ATTR) CurrentUser user,
                                     @Valid @RequestBody ApplicationRequest r) {
        if (user.isStudent() && !user.studentId().equals(r.studentId())) {
            throw ApiException.forbidden("You can only apply on your own behalf");
        }
        return service.apply(r);
    }

    @PutMapping("/{id}/status")
    public ApplicationResponse updateStatus(@RequestAttribute(AuthInterceptor.ATTR) CurrentUser user, @PathVariable Long id,
                                            @Valid @RequestBody StatusUpdateRequest r) {
        if (!user.isAdmin()) throw ApiException.forbidden("Only the placement officer can update status");
        return service.updateStatus(id, r.status(), r.remarks());
    }

    @PutMapping("/{id}/withdraw")
    public ApplicationResponse withdraw(@RequestAttribute(AuthInterceptor.ATTR) CurrentUser user, @PathVariable Long id) {
        return service.withdraw(id, user.isStudent() ? user.studentId() : null);
    }
}
