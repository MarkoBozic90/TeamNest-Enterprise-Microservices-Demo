package com.teamnest.shared.problem;

import java.net.URI;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProblemTypes {


    public static final URI VALIDATION = URI.create("https://teamnest.dev/problems/validation");
    public static final URI RATE_LIMIT = URI.create("https://teamnest.dev/problems/rate-limit");
    public static final URI AUTH = URI.create("https://teamnest.dev/problems/authentication");
    public static final URI FORBIDDEN = URI.create("https://teamnest.dev/problems/forbidden");
    public static final URI DOWNSTREAM = URI.create("https://teamnest.dev/problems/downstream");
    public static final URI INTERNAL = URI.create("https://teamnest.dev/problems/internal");
}

