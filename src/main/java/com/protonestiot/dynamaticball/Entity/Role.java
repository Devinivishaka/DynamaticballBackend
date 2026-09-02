package com.protonestiot.dynamaticball.Entity;

import lombok.Getter;

@Getter
public enum Role {
    SUPER_ADMIN("Super Admin"),
    REFEREE("Referee"),
    VIDEO_ADMIN("Video Admin");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public static String formatRole(Role role) {
        return role != null && role.getDisplayName() != null ? role.getDisplayName() : "User";
    }
}
