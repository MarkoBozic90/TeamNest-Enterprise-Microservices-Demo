package com.teamnest.shared.problem;

import static com.teamnest.shared.problem.ProblemTypes.AUTH;
import static com.teamnest.shared.problem.ProblemTypes.DOWNSTREAM;
import static com.teamnest.shared.problem.ProblemTypes.FORBIDDEN;
import static com.teamnest.shared.problem.ProblemTypes.INTERNAL;
import static com.teamnest.shared.problem.ProblemTypes.RATE_LIMIT;
import static com.teamnest.shared.problem.ProblemTypes.VALIDATION;

import java.net.URI;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ErrorTypeMapper {

    public static URI toType(ErrorCode code) {
        return switch (code) {
            case AUTHENTICATION_FAILED  -> AUTH;
            case ACCESS_FORBIDDEN       -> FORBIDDEN;
            case VALIDATION_ERROR       -> VALIDATION;
            case RATE_LIMIT_EXCEEDED    -> RATE_LIMIT;
            case DOWNSTREAM_UNAVAILABLE -> DOWNSTREAM;
            default                     -> INTERNAL;
        };
    }
}