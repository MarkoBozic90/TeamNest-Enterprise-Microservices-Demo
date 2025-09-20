package com.teamnest.gateway.test.unit;

import static com.teamnest.gateway.constant.StringConstant.APP;
import static com.teamnest.gateway.constant.StringConstant.DETAIL_BAD;
import static com.teamnest.gateway.constant.StringConstant.DETAIL_H2;
import static com.teamnest.gateway.constant.StringConstant.DETAIL_OOPS;
import static com.teamnest.gateway.constant.StringConstant.ERROR_CODE;
import static com.teamnest.gateway.constant.StringConstant.GATEWAY;
import static com.teamnest.gateway.constant.StringConstant.HINT;
import static com.teamnest.gateway.constant.StringConstant.NOPE;
import static com.teamnest.gateway.constant.StringConstant.PATH_ANY;
import static com.teamnest.gateway.constant.StringConstant.PATH_LOGIN;
import static com.teamnest.gateway.constant.StringConstant.PATH_RATE_LIMITED;
import static com.teamnest.gateway.constant.StringConstant.PATH_SECURE;
import static com.teamnest.gateway.constant.StringConstant.PATH_SVC;
import static com.teamnest.gateway.constant.StringConstant.REQUEST_ID;
import static com.teamnest.gateway.constant.StringConstant.RETRY_AFTER_SECONDS;
import static com.teamnest.gateway.constant.StringConstant.RID_123;
import static com.teamnest.gateway.constant.StringConstant.RID_429;
import static com.teamnest.gateway.constant.StringConstant.RID_999;
import static com.teamnest.gateway.constant.StringConstant.RID_COMM;
import static com.teamnest.gateway.constant.StringConstant.RID_X;
import static com.teamnest.gateway.constant.StringConstant.X;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;

import com.teamnest.gateway.error.GlobalExceptionHandler;
import com.teamnest.gateway.error.ProblemFactory;
import com.teamnest.gateway.trace.RequestIdFilterConfig;
import com.teamnest.shared.problem.ErrorCode;
import java.net.URI;
import java.util.Locale;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

/**
 * Testira  direktno metode exception handler-a (junit).
 * Pokrivamo: auth, forbidden, status-based (4xx i 5xx), generic, committed response.
 */
class GlobalExceptionHandlerUnitTest {
    private ProblemFactory pf;
    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        pf = mock(ProblemFactory.class);

        // univerzalni stub: vrati PD sa traženim statusom + minimalnim propertiјima
        when(pf.build(any(), any(), any(), any(), any())).thenAnswer(inv -> {
            ErrorCode code = inv.getArgument(0);
            var status     = inv.getArgument(1, org.springframework.http.HttpStatus.class);
            String detail  = inv.getArgument(2);
            ServerWebExchange ex = inv.getArgument(4);

            ProblemDetail pd = ProblemDetail.forStatus(status);
            pd.setTitle(code.name());
            pd.setDetail(detail == null ? "" : detail);
            pd.setProperty(ERROR_CODE, code.getCode());
            pd.setProperty(APP, GATEWAY);

            // postavi instance i requestId kao što bi realna fabrika radila
            pd.setInstance(URI.create(ex.getRequest().getPath().value()));
            String rid = ex.getResponse().getHeaders().getFirst(RequestIdFilterConfig.HEADER);
            if (rid == null || rid.isBlank()) {
                rid = ex.getRequest().getHeaders().getFirst(RequestIdFilterConfig.HEADER);
            }
            if (rid != null) {
                pd.setProperty(REQUEST_ID, rid);
            }
            return pd;
        });

        handler = new GlobalExceptionHandler(pf);
    }

    private MockServerWebExchange exReqRid(String path, String rid) {
        return MockServerWebExchange.from(
            MockServerHttpRequest.get(path).header(RequestIdFilterConfig.HEADER, rid)
        );
    }

    private MockServerWebExchange exRespRid(String path, String rid) {
        var ex = MockServerWebExchange.from(MockServerHttpRequest.get(path));
        ex.getResponse().getHeaders().add(RequestIdFilterConfig.HEADER, rid);
        return ex;
    }

    // ---------- AUTH 401: normalna grana + committed grana ----------
    @Test
    void handleAuth_returns_401_and_hint_and_propagates_requestId() {
        var ex = exReqRid(PATH_LOGIN, RID_123);
        ResponseEntity<ProblemDetail> resp =
            handler.handleAuth(new BadCredentialsException(DETAIL_BAD), Locale.US, ex);

        assertThat(resp.getStatusCode()).isEqualTo(UNAUTHORIZED);
        assertThat(resp.getHeaders().getFirst(RequestIdFilterConfig.HEADER)).isEqualTo(RID_123);

        var pd = resp.getBody();
        assertThat(pd).isNotNull();
        assertThat(pd.getDetail()).isEqualTo(DETAIL_BAD);
        assertThat(pd.getProperties()).containsEntry(ERROR_CODE, ErrorCode.AUTHENTICATION_FAILED.getCode());
        assertThat(pd.getProperties()).containsEntry(REQUEST_ID, RID_123);
        assertThat(pd.getProperties()).containsEntry(APP, GATEWAY);
        assertThat(pd.getProperties()).containsKey(HINT);
        Assertions.assertNotNull(pd.getInstance());
        assertThat(pd.getInstance().getPath()).isEqualTo(PATH_LOGIN);
    }

    @Test
    void handleAuth_when_response_committed_rethrows_original_exception() {
        var ex = exReqRid(PATH_LOGIN, RID_COMM);
        ex.getResponse().setComplete().block(); // markiraj committed

        var badCreds = new BadCredentialsException(DETAIL_BAD);
        var us = Locale.US;

        assertThatThrownBy(() -> handler.handleAuth(badCreds, us, ex))
            .isExactlyInstanceOf(BadCredentialsException.class);
    }

    // ---------- FORBIDDEN 403 ----------
    @Test
    void handleForbidden_403_and_propagates_response_header_requestId() {
        var ex = exRespRid(PATH_SECURE, RID_999);
        var resp = handler.handleForbidden(new AccessDeniedException(NOPE), Locale.getDefault(), ex);

        assertThat(resp.getStatusCode()).isEqualTo(FORBIDDEN);
        assertThat(resp.getHeaders().getFirst(RequestIdFilterConfig.HEADER)).isEqualTo(RID_999);

        var pd = resp.getBody();
        assertThat(pd).isNotNull();
        assertThat(pd.getProperties()).containsEntry(ERROR_CODE, ErrorCode.ACCESS_FORBIDDEN.getCode());
        assertThat(pd.getProperties()).containsEntry(REQUEST_ID, RID_999);
    }

    // ---------- STATUS-based: 5xx, 4xx, ostalo ----------
    @Test
    void handleStatus_5xx_maps_code_DOWNSTREAM_UNAVAILABLE_and_preserves_500() {
        var ex = exReqRid(PATH_SVC, X);
        var resp = handler.handleStatus(new ResponseStatusException(INTERNAL_SERVER_ERROR, DETAIL_OOPS),
            Locale.getDefault(), ex);

        assertThat(resp.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);

        // verifikuj koji je kod handler prosledio fabrici
        var codeCap = ArgumentCaptor.forClass(ErrorCode.class);
        verify(pf).build(codeCap.capture(), eq(INTERNAL_SERVER_ERROR), eq(DETAIL_OOPS), any(), any());
        assertThat(codeCap.getValue()).isEqualTo(ErrorCode.DOWNSTREAM_UNAVAILABLE);

        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getDetail()).isEqualTo(DETAIL_OOPS);
    }

    @Test
    void handleStatus_4xx_maps_code_UPSTREAM_CLIENT_ERROR_and_preserves_422() {
        var ex = exReqRid(PATH_SVC, X);
        var resp = handler.handleStatus(new ErrorResponseException(UNPROCESSABLE_ENTITY),
            Locale.getDefault(), ex);

        assertThat(resp.getStatusCode()).isEqualTo(UNPROCESSABLE_ENTITY);
        var codeCap = ArgumentCaptor.forClass(ErrorCode.class);
        verify(pf).build(codeCap.capture(), eq(UNPROCESSABLE_ENTITY), any(), any(), any());
        assertThat(codeCap.getValue()).isEqualTo(ErrorCode.UPSTREAM_CLIENT_ERROR);
    }

    @Test
    void handleStatus_other_preserves_status_and_maps_to_INTERNAL_ERROR() {
        var ex = exReqRid(PATH_SVC, X);
        var resp = handler.handleStatus(new ResponseStatusException(SWITCHING_PROTOCOLS, DETAIL_H2), Locale.US, ex);

        assertThat(resp.getStatusCode()).isEqualTo(SWITCHING_PROTOCOLS);
        var codeCap = ArgumentCaptor.forClass(ErrorCode.class);
        verify(pf).build(codeCap.capture(), eq(SWITCHING_PROTOCOLS), any(), any(), any());
        assertThat(codeCap.getValue()).isEqualTo(ErrorCode.INTERNAL_ERROR);
    }

    // ---------- Retry-After grana (429) ----------
    @Test
    void respond_adds_retry_after_header_on_429() {
        var ex = exReqRid( PATH_RATE_LIMITED, RID_429);
        var resp = handler.handleStatus(new ResponseStatusException(TOO_MANY_REQUESTS, "rl"), Locale.US, ex);

        assertThat(resp.getStatusCode()).isEqualTo(TOO_MANY_REQUESTS);
        assertThat(resp.getHeaders().getFirst(HttpHeaders.RETRY_AFTER)).isEqualTo(RETRY_AFTER_SECONDS);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getProperties()).containsEntry("retryAfter", RETRY_AFTER_SECONDS);
    }

    // ---------- Throwable fallback + committed branch ----------
    @Test
    void handleOther_builds_problem_with_INTERNAL_ERROR_and_cause() {
        var ex = exReqRid(PATH_ANY, RID_X);
        var resp = handler.handleOther(new IllegalArgumentException(DETAIL_BAD), Locale.getDefault(), ex);
        assertThat(resp.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(resp.getBody().getProperties()).containsEntry("cause", "IllegalArgumentException");
    }

    @Test
    void handleOther_when_committed_rethrows() {
        var ex = exReqRid(PATH_ANY, RID_X);
        ex.getResponse().setComplete().block();
        var committed = new IllegalStateException("committed");
        var loc = Locale.getDefault();

        assertThatThrownBy(() -> handler.handleOther(committed, loc, ex))
            .isExactlyInstanceOf(IllegalStateException.class);

    }
}