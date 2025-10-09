package com.teamnest.userservice.exception;

public class InviteExpiredException extends RuntimeException {

    public InviteExpiredException() {
        super("Invite expired");
    }

    public InviteExpiredException(String msg) {
        super(msg);
    }
}
