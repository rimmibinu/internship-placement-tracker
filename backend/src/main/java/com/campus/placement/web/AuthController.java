package com.campus.placement.web;

import com.campus.placement.dto.LoginRequest;
import com.campus.placement.exception.ApiException;
import com.campus.placement.repository.StudentRepository;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final StudentRepository students;
    private final String adminEmail;
    private final boolean demoMode;

    public AuthController(StudentRepository students, @Value("${app.admin-email}") String adminEmail,
                          @Value("${app.demo-mode:false}") boolean demoMode) {
        this.students = students;
        this.adminEmail = adminEmail;
        this.demoMode = demoMode;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest req) {
        String email = req.email().trim();
        Map<String, Object> out = new LinkedHashMap<>();
        if (email.equalsIgnoreCase(adminEmail)) {
            out.put("role", "ADMIN");
            out.put("name", "Placement Officer");
            out.put("email", adminEmail);
            out.put("studentId", null);
            return out;
        }
        var s = students.findByEmailIgnoreCase(email).orElseThrow(() -> ApiException.unauthorized("No account found for this email"));
        out.put("role", "STUDENT");
        out.put("name", s.getName());
        out.put("email", s.getEmail());
        out.put("studentId", s.getId());
        return out;
    }

    /** Demo helper for the login screen (disabled when app.demo-mode=false). */
    @GetMapping("/demo-users")
    public List<Map<String, String>> demoUsers() {
        List<Map<String, String>> users = new ArrayList<>();
        if (!demoMode) return users;
        users.add(Map.of("role", "ADMIN", "name", "Placement Officer", "email", adminEmail));
        students.findAll().stream().limit(5).forEach(s ->
                users.add(Map.of("role", "STUDENT", "name", s.getName(), "email", s.getEmail())));
        return users;
    }
}
