package com.teamnest.userservice.exception;

public class InviteNotFoundException extends RuntimeException {
    public InviteNotFoundException() {
        super("Invite not found");
    }
}