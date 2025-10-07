package com.teamnest.userservice.infra;

import com.teamnest.userservice.port.TokenHasher;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class Sha256TokenHasher implements TokenHasher {
    @Override
    public String hash(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] out = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(out.length * 2);
            for (byte b : out) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (Exception e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}