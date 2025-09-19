package com.teamnest.shared.problem;

import java.io.Serializable;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;



@Getter
public class ServiceException extends RuntimeException implements Serializable {

    private final ErrorCode code;
    private final Map<String, Object> details;

    @Builder
    public ServiceException(ErrorCode code, String message, Map<String, Object> details) {
        super(message);
        this.code = code;
        this.details = details;
    }

    @Builder(builderMethodName = "withCause",
        builderClassName = "CauseBuilder")
    public ServiceException(final Throwable cause, final ErrorCode code, final Map<String, Object> details) {
        super(cause);
        this.code = code;
        this.details = details;
    }
}

