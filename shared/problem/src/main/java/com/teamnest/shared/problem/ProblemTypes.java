package com.teamnest.shared.problem;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProblemTypes {

    public static final String USER_DUPLICATE_EMAIL = "urn:problem:user/duplicate-email";
    public static final String VALIDATION_ERROR     = "urn:problem:common/validation";
    public static final String INTERNAL_ERROR       = "urn:problem:common/internal";
}
