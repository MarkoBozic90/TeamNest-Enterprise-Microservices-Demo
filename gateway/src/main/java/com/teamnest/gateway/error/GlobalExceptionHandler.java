package com.teamnest.gateway.error;


import com.teamnest.shared.problem.ErrorCode;
import com.teamnest.shared.problem.ErrorTypeMapper;
import com.teamnest.shared.problem.ProblemTypes;
import com.teamnest.shared.problem.ServiceException;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Locale;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final MessageSource messages;

    public GlobalExceptionHandler(MessageSource messages) {
        this.messages = messages;
    }

    // ---- domain/service exceptions (naš format) ----
    @ExceptionHandler(ServiceException.class)
    ProblemDetail handleService(ServiceException ex, Locale locale) {
        int status = ex.getCode().httpStatus();
        var pd = ProblemDetail.forStatus(status);
        pd.setType(ErrorTypeMapper.toType(ex.getCode()));
       // pd.setTitle(messageSource.getMessage(ex.getCode().i18nKey(), null, defaultTitle(ex), locale));
        pd.setDetail(ex.getMessage());
        pd.setProperty("errorCode", ex.getCode().code());
        // add timestamp/app/correlationId if you like
        return pd;
    }

    // ---- auth / access ----
    @ExceptionHandler(AuthenticationException.class)
    ProblemDetail handleAuth(AuthenticationException ex, Locale locale) {
        var pd = newProblem(HttpStatus.UNAUTHORIZED, ProblemTypes.AUTH,
            msg("problem.unauthenticated", "Authentication required", locale));
        pd.setProperty("hint", "Provide a valid Bearer token");
        return pd;
    }

    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail handleForbidden(AccessDeniedException ex, Locale locale) {
        return newProblem(HttpStatus.FORBIDDEN, ProblemTypes.FORBIDDEN,
            msg("problem.forbidden", "You don't have permission to access this resource", locale));
    }

    // ---- gateway/downstream / generic status ----
    @ExceptionHandler({ResponseStatusException.class, ErrorResponseException.class})
    ProblemDetail handleStatus(RuntimeException ex, Locale locale) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        if (ex instanceof ResponseStatusException rse) status = HttpStatus.valueOf(rse.getStatusCode().value());
        if (ex instanceof ErrorResponseException ere) status = HttpStatus.valueOf(ere.getStatusCode().value());

        URI type = (status.is5xxServerError()) ? ProblemTypes.DOWNSTREAM : ProblemTypes.INTERNAL;
        String titleKey = status.is5xxServerError() ? "problem.downstream" : "problem.internal";
        return newProblem(status, type, msg(titleKey, status.getReasonPhrase(), locale));
    }

    // ---- fallback ----
    @ExceptionHandler(Throwable.class)
    ProblemDetail handleOther(Throwable ex, Locale locale) {
        var pd = newProblem(HttpStatus.INTERNAL_SERVER_ERROR, ProblemTypes.INTERNAL,
            msg("problem.internal", "Unexpected error", locale));
        pd.setProperty("cause", ex.getClass().getSimpleName());
        return pd;
    }

    // ---- helpers ----
    private record Mapping(HttpStatus status, URI type) { }
    private Mapping mapCode(ErrorCode code) {
        return switch (code) {
            case VALIDATION_ERROR -> new Mapping(HttpStatus.BAD_REQUEST, ProblemTypes.VALIDATION);
            case RATE_LIMIT_EXCEEDED -> new Mapping(HttpStatus.TOO_MANY_REQUESTS, ProblemTypes.RATE_LIMIT);
//            case UNAUTHENTICATED -> new Mapping(HttpStatus.UNAUTHORIZED, ProblemTypes.AUTH);
//            case FORBIDDEN -> new Mapping(HttpStatus.FORBIDDEN, ProblemTypes.FORBIDDEN);
            case DOWNSTREAM_UNAVAILABLE -> new Mapping(HttpStatus.SERVICE_UNAVAILABLE, ProblemTypes.DOWNSTREAM);
            default -> new Mapping(HttpStatus.INTERNAL_SERVER_ERROR, ProblemTypes.INTERNAL);
        };
    }

    private ProblemDetail newProblem(HttpStatus status, URI type, String title) {
        var pd = ProblemDetail.forStatusAndDetail(status, title);
        pd.setType(type);
        pd.setTitle(title);
        pd.setProperty("timestamp", OffsetDateTime.now().toString());
        pd.setProperty("app", "gateway");
        return pd;
    }

    private String msg(String key, String fallback, Locale locale) {
        try { return messages.getMessage(key, null, locale); }
        catch (Exception ignore) { return fallback; }
    }
}
