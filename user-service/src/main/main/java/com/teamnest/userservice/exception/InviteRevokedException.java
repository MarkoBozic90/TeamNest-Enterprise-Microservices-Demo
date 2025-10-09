package com.teamnest.userservice.exception;

public class InviteRevokedException extends RuntimeException {

    public InviteRevokedException() {
        super("Invite revoked");
    }
}
