package com.teamnest.userservice.infra;


import com.teamnest.userservice.port.TokenGenerator;
import java.security.SecureRandom;
import java.util.Base64;

public class SecureRandomTokenGenerator implements TokenGenerator {
    private static final SecureRandom RNG = new SecureRandom();

    @Override
    public String generate() {
        byte[] buf = new byte[32];
        RNG.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }
}