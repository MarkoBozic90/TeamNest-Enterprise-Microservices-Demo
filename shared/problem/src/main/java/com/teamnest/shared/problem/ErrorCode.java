package com.teamnest.shared.problem;


import java.net.URI;
import lombok.Getter;

/**
 * Stable error catalog for all services.
 * - code: business-stable error id (prefix per bounded context)
 * - httpStatus: numeric HTTP status to avoid Spring coupling in shared lib
 * - i18nKey: message key resolved by MessageSource in each app
 */

@Getter
public enum ErrorCode {
    AUTHENTICATION_FAILED("KC-001", 401, "error.auth.failed", ProblemTypes.AUTH),
    ACCESS_FORBIDDEN("GW-001", 403, "error.forbidden", ProblemTypes.FORBIDDEN),
    VALIDATION_ERROR("CMN-001", 400, "error.validation", ProblemTypes.VALIDATION),
    RATE_LIMIT_EXCEEDED("GW-002", 429, "error.rateLimit", ProblemTypes.RATE_LIMIT),
    DOWNSTREAM_UNAVAILABLE("GW-003", 503, "error.downstream", ProblemTypes.DOWNSTREAM),
    INTERNAL_ERROR("CMN-999", 500, "error.internal", ProblemTypes.INTERNAL),
    UPSTREAM_CLIENT_ERROR("GW-004", 400, "54", ProblemTypes.AUTH);

    private final String code;
    private final int httpStatus;
    private final String i18nKey;
    private final URI problemType;

    ErrorCode(String code, int httpStatus, String i18nKey, URI problemType) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.i18nKey = i18nKey;
        this.problemType = problemType;
    }

}
