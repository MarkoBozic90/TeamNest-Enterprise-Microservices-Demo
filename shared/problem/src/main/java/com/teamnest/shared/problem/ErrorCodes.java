package com.teamnest.shared.problem;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorCodes {
    public static final String USER_DUPLICATE_EMAIL = "error.user.duplicateEmail";
    public static final String VALIDATION_ERROR     = "error.common.validation";
    public static final String INTERNAL_ERROR       = "error.common.internal";
}
