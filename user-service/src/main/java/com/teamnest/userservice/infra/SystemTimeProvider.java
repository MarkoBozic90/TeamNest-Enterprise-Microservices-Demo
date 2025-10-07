package com.teamnest.userservice.infra;


import com.teamnest.userservice.port.TimeProvider;
import java.time.Instant;

public class SystemTimeProvider implements TimeProvider {
    @Override
    public Instant now() {
        return Instant.now();
    }
}