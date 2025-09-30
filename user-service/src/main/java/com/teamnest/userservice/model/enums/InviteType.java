package com.teamnest.userservice.model.enums;

import java.util.UUID;

public enum InviteType {
    ROLE_PLAYER("/players", false, 200),
    ROLE_STAFF("/pending/staff", true, 20),
    ROLE_PARENT("/parents", false, 200);

    private final String defaultGroupPath;
    private final boolean requiresApproval;
    private final int defaultMaxUses;

    InviteType(String defaultGroupPath, boolean requiresApproval, int defaultMaxUses) {
        this.defaultGroupPath = defaultGroupPath;
        this.requiresApproval = requiresApproval;
        this.defaultMaxUses = defaultMaxUses;
    }

    public String defaultGroupPath(UUID clubId) {
        if (defaultGroupPath.startsWith("/clubs/")) {
            return defaultGroupPath;
        }
        return "/clubs/" + clubId + defaultGroupPath;
    }

    boolean requiresApproval() {
        return requiresApproval;
    }

    int defaultMaxUses() {
        return defaultMaxUses;
    }
}
