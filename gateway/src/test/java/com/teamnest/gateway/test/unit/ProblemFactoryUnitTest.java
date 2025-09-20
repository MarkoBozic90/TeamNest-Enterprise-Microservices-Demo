package com.teamnest.gateway.test.unit;

import static com.teamnest.gateway.constant.StringConstant.APP;
import static com.teamnest.gateway.constant.StringConstant.ERROR_CODE;
import static com.teamnest.gateway.constant.StringConstant.GATEWAY;
import static com.teamnest.gateway.constant.StringConstant.PATH_X;
import static com.teamnest.gateway.constant.StringConstant.PATH_Y;
import static com.teamnest.gateway.constant.StringConstant.PATH_Z;
import static com.teamnest.gateway.constant.StringConstant.REQUEST_ID;
import static com.teamnest.gateway.constant.StringConstant.RID_1;
import static com.teamnest.gateway.constant.StringConstant.RID_REQ;
import static com.teamnest.gateway.constant.StringConstant.X_DETAIL;
import static org.assertj.core.api.Assertions.*;

import com.teamnest.gateway.error.ProblemFactory;
import com.teamnest.gateway.trace.RequestIdFilterConfig;
import com.teamnest.shared.problem.ErrorCode;
import java.util.Locale;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

class ProblemFactoryUnitTest {

    private MessageSource goodMS() {
        StaticMessageSource sms = new StaticMessageSource();
        // pokrijemo bar dva i18n ključa
        sms.addMessage("error.internal", Locale.ENGLISH, "Internal boom");
        sms.addMessage("error.downstream", Locale.ENGLISH, "Upstream down");
        return sms;
    }

    private MessageSource throwingMS() {
        return new MessageSource() {
            @Override public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
                throw new NoSuchMessageException(code);
            }
            @Override public String getMessage(String code, Object[] args, Locale locale) {
                throw new NoSuchMessageException(code);
            }
            @Override public String getMessage(org.springframework.context.MessageSourceResolvable resolvable, Locale locale) {
                throw new NoSuchMessageException("x");
            }
        };
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

    @Test
    void build_uses_message_source_title_and_sets_type_instance_requestId_and_props() {
        var factory = new ProblemFactory(goodMS());
        var ex = exRespRid(PATH_X, RID_1);

        ProblemDetail pd = factory.build(
            ErrorCode.DOWNSTREAM_UNAVAILABLE,
            HttpStatus.SERVICE_UNAVAILABLE,
            X_DETAIL,
            Locale.ENGLISH,
            ex
        );

        assertThat(pd.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE.value());
        assertThat(pd.getTitle()).isEqualTo("Upstream down");      // iz MS
        assertThat(pd.getDetail()).isEqualTo(X_DETAIL);
        assertThat(pd.getType()).isNotNull();                      // iz code.getProblemType()
        Assertions.assertNotNull(pd.getInstance());
        assertThat(pd.getInstance().getPath()).isEqualTo(PATH_X);   // path
        assertThat(pd.getProperties()).containsEntry(ERROR_CODE, ErrorCode.DOWNSTREAM_UNAVAILABLE.getCode());
        assertThat(pd.getProperties()).containsEntry(APP, GATEWAY);
        assertThat(pd.getProperties()).containsEntry(REQUEST_ID, RID_1);
    }

    @Test
    void build_falls_back_when_message_missing_and_handles_null_detail_and_request_header_rid() {
        var factory = new ProblemFactory(throwingMS());
        var ex = exReqRid(PATH_Y, RID_REQ);

        ProblemDetail pd = factory.build(
            ErrorCode.INTERNAL_ERROR,
            HttpStatus.INTERNAL_SERVER_ERROR,
            null,                    // detail null → očekujemo ""
            Locale.ENGLISH,
            ex
        );

        assertThat(pd.getTitle()).isNotBlank();                    // fallback (defaultTitle)
        assertThat(pd.getDetail()).isEmpty();                  // null → ""
        assertThat(pd.getProperties()).containsEntry(REQUEST_ID, RID_REQ);
    }

    @Test
    void build_when_no_requestId_present_sets_property_present_with_null_or_omits_based_on_impl() {
        var factory = new ProblemFactory(goodMS());
        var ex = MockServerWebExchange.from(MockServerHttpRequest.get(PATH_Z));

        ProblemDetail pd = factory.build(
            ErrorCode.INTERNAL_ERROR,
            HttpStatus.INTERNAL_SERVER_ERROR,
            "boom",
            Locale.ENGLISH,
            ex
        );

        // dozvoli obe implementacije: ili ostavi key sa null, ili izostavi key
        var props = pd.getProperties();
        assertThat(props.containsKey(REQUEST_ID) ? props.get(REQUEST_ID) : null).isNull();
    }
}
