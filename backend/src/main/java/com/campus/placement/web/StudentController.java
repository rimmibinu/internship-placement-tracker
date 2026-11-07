package com.campus.placement.web;

import com.campus.placement.config.AuthInterceptor;
import com.campus.placement.config.CurrentUser;
import com.campus.placement.dto.StudentRequest;
import com.campus.placement.dto.StudentResponse;
import com.campus.placement.exception.ApiException;
import com.campus.placement.service.StudentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/students")
public class StudentController {
    private final StudentService service;

    public StudentController(StudentService service) { this.service = service; }

    @GetMapping
    public List<StudentResponse> list(@RequestParam(defaultValue = "") String q,
                                      @RequestParam(defaultValue = "") String department) {
        return service.list(q, department);
    }

    @GetMapping("/{id}")
    public StudentResponse get(@RequestAttribute(AuthInterceptor.ATTR) CurrentUser user, @PathVariable Long id) {
        if (user.isStudent() && !id.equals(user.studentId())) {
            throw ApiException.forbidden("You can only view your own profile");
        }
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudentResponse create(@Valid @RequestBody StudentRequest r) { return service.create(r); }

    @PutMapping("/{id}")
    public StudentResponse update(@PathVariable Long id, @Valid @RequestBody StudentRequest r) { return service.update(id, r); }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }
}
