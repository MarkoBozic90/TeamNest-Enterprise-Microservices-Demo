package com.teamnest.userservice.port;

import java.time.Instant;

public interface TimeProvider {
    Instant now();
}
