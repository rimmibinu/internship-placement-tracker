package com.campus.placement.config;

import com.campus.placement.domain.Student;
import com.campus.placement.exception.ApiException;
import com.campus.placement.repository.StudentRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Lightweight demo authentication: the client sends the X-User-Email header.
 * The admin (placement officer) email is configured; everyone else must be a registered student.
 * NOTE: no passwords by design (no secrets in the repo). For production replace with Spring Security + OAuth/JWT.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {
    public static final String ATTR = "currentUser";
    private final StudentRepository students;
    private final String adminEmail;

    public AuthInterceptor(StudentRepository students, @Value("${app.admin-email}") String adminEmail) {
        this.students = students;
        this.adminEmail = adminEmail;
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) return true;
        String path = req.getRequestURI();
        if (path.startsWith("/api/auth/")) return true;

        String email = req.getHeader("X-User-Email");
        if (email == null || email.isBlank()) throw ApiException.unauthorized("Please log in");

        CurrentUser user;
        if (email.trim().equalsIgnoreCase(adminEmail)) {
            user = new CurrentUser(CurrentUser.Role.ADMIN, adminEmail, null);
        } else {
            Student s = students.findByEmailIgnoreCase(email.trim())
                    .orElseThrow(() -> ApiException.unauthorized("Unknown user"));
            user = new CurrentUser(CurrentUser.Role.STUDENT, s.getEmail(), s.getId());
        }

        if (user.isStudent()) {
            String m = req.getMethod();
            boolean get = "GET".equalsIgnoreCase(m);
            if (path.startsWith("/api/dashboard") || (get && path.equals("/api/students"))
                    || (!get && !path.startsWith("/api/applications"))
                    || path.matches("/api/applications/\\d+/status")) {
                throw ApiException.forbidden("This action requires the placement officer role");
            }
        }
        req.setAttribute(ATTR, user);
        return true;
    }
}
