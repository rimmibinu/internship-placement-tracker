package com.campus.placement.domain;

import java.util.EnumSet;
import java.util.Set;

/** Application lifecycle. Allowed transitions are part of the domain rules. */
public enum ApplicationStatus {
    APPLIED, SHORTLISTED, INTERVIEW, OFFERED, REJECTED, WITHDRAWN;

    public Set<ApplicationStatus> allowedNext() {
        return switch (this) {
            case APPLIED -> EnumSet.of(SHORTLISTED, REJECTED, WITHDRAWN);
            case SHORTLISTED -> EnumSet.of(INTERVIEW, REJECTED, WITHDRAWN);
            case INTERVIEW -> EnumSet.of(OFFERED, REJECTED, WITHDRAWN);
            default -> EnumSet.noneOf(ApplicationStatus.class);
        };
    }

    public boolean canMoveTo(ApplicationStatus target) {
        return allowedNext().contains(target);
    }
}
