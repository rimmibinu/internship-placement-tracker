package com.campus.placement.config;

public record CurrentUser(Role role, String email, Long studentId) {
    public enum Role { ADMIN, STUDENT }
    public boolean isAdmin() { return role == Role.ADMIN; }
    public boolean isStudent() { return role == Role.STUDENT; }
}
