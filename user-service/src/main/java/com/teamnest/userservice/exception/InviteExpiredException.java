package com.teamnest.userservice.exception;

import java.util.UUID;

public class InviteExpiredException  extends RuntimeException {
    @SuppressWarnings("checkstyle:WhitespaceAfter")
    public InviteExpiredException(UUID uuid) {
        super("Invite expired Exception withe id " + uuid);

    }
}
