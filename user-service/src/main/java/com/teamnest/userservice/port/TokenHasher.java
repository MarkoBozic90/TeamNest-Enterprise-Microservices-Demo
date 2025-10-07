package com.teamnest.userservice.port;

public interface TokenHasher {
    String hash(String raw);
}