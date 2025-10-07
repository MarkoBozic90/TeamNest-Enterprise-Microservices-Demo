package com.teamnest.userservice.exception;

import java.util.UUID;

public class InviteExhaustedException extends RuntimeException {
    public InviteExhaustedException(UUID id) {
        super("Invite uses exhausted: " + id);
    }
}