package com.teamnest.gateway.error;

import com.teamnest.gateway.trace.RequestIdFilterConfig;
import com.teamnest.shared.problem.ErrorCode;
import com.teamnest.shared.problem.ServiceException;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final ProblemFactory problems;

    @ExceptionHandler(ServiceException.class)
    ResponseEntity<ProblemDetail> handleService(
        final ServiceException ex,
        final  Locale loc,
        final  ServerWebExchange exg) {
        if (exg.getResponse().isCommitted()) {
            throw ex;
        }
        var pd = problems.build(ex.getCode(),
            HttpStatus.valueOf(ex.getCode().getHttpStatus()), // ili ex.getStatus() ako ima
            ex.getMessage(), loc, exg);
        if (ex.getDetails() != null) {
            pd.setProperty("details", ex.getDetails());
        }
        return respond(pd, exg);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuth(
        final AuthenticationException ex,
        final Locale loc,
        final ServerWebExchange exg) {
        if (exg.getResponse().isCommitted()) {
            throw ex;
        }
        var pd = problems.build(ErrorCode.AUTHENTICATION_FAILED, HttpStatus.UNAUTHORIZED, ex.getMessage(), loc, exg);
        pd.setProperty("hint", "Provide a valid Bearer token");
        return respond(pd, exg);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleForbidden(AccessDeniedException ex, Locale loc, ServerWebExchange exg) {
        if (exg.getResponse().isCommitted()) {
            throw ex;
        }
        var pd = problems.build(ErrorCode.ACCESS_FORBIDDEN, HttpStatus.FORBIDDEN, ex.getMessage(), loc, exg);
        return respond(pd, exg);
    }

    // Status-based; čuva ORIGINALNI status, bira "kategoriju"
    @ExceptionHandler({ResponseStatusException.class, ErrorResponseException.class})
    public ResponseEntity<ProblemDetail> handleStatus(
        final RuntimeException ex,
        final Locale loc,
        final ServerWebExchange exg) {
        if (exg.getResponse().isCommitted()) {
            throw ex;
        }

        HttpStatus status;
        String detail = null;

        if (ex instanceof ResponseStatusException rse) {
            status = HttpStatus.valueOf(rse.getStatusCode().value());
            detail = java.util.Objects.toString(rse.getReason(), "");
        }
        else if (ex instanceof ErrorResponseException ere) {
            status = HttpStatus.valueOf(ere.getStatusCode().value());
            var body = ere.getBody();
            detail = java.util.Objects.toString(body.getDetail(), "");
        }
        else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            detail = "";
        }

        final ErrorCode code;

        if (status.is5xxServerError()) {
            code = ErrorCode.DOWNSTREAM_UNAVAILABLE;
        }
        else if (status.is4xxClientError()) {
            code = ErrorCode.UPSTREAM_CLIENT_ERROR;
        }
        else {
            code = ErrorCode.INTERNAL_ERROR;
        }


        var pd = problems.build(code, status, detail, loc, exg);
        return respond(pd, exg);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ProblemDetail> handleOther(
        final Throwable ex,
        final Locale loc,
        final ServerWebExchange exg) {
        if (exg.getResponse().isCommitted()) {
            if (ex instanceof RuntimeException re) {
                throw re;
            }
            throw new IllegalStateException("Response already committed", ex);
        }
        var pd = problems.build(ErrorCode.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), loc, exg);
        pd.setProperty("cause", ex.getClass().getSimpleName());
        return respond(pd, exg);
    }

    private ResponseEntity<ProblemDetail> respond(final ProblemDetail pd, final ServerWebExchange exchange) {
        var b = ResponseEntity.status(pd.getStatus())
            .contentType(MediaType.APPLICATION_PROBLEM_JSON);

        var rid = exchange.getResponse().getHeaders().getFirst(RequestIdFilterConfig.HEADER);
        if (rid == null || rid.isBlank()) {
            rid = exchange.getRequest().getHeaders().getFirst(RequestIdFilterConfig.HEADER);
        }
        if (rid != null && !rid.isBlank()) {
            b.header(RequestIdFilterConfig.HEADER, rid);
        }

        if (pd.getStatus() == HttpStatus.TOO_MANY_REQUESTS.value()) {
            b.header(HttpHeaders.RETRY_AFTER, "5");
            pd.setProperty("retryAfter", "5");
        }
        return b.body(pd);
    }
}