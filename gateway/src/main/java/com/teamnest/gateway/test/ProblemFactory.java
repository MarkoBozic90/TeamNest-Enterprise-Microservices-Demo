package com.teamnest.gateway.test;

import com.teamnest.gateway.trace.RequestIdFilterConfig;
import com.teamnest.shared.problem.ErrorCode;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProblemFactory {

    private final MessageSource messages;

    public ProblemDetail build(final ErrorCode code,
                               final HttpStatus status,
                               final String detail,
                               final Locale locale,
                               final ServerWebExchange exchange) {

        ProblemDetail pd = ProblemDetail.forStatus(status);

        if (code.getProblemType() != null) {
            try {
                pd.setType(URI.create(code.getProblemType().toString()));
            }
            catch (Exception ignore) {
                log.error(code.getProblemType().toString(), ignore);

            }
        }

        String title;
        try {
            title = messages.getMessage(code.getI18nKey(), null, defaultTitle(code), locale);
        }
        catch (Exception e) {
            title = defaultTitle(code);
        }
        pd.setTitle(title);

        pd.setDetail(detail == null ? "" : detail);
        pd.setProperty("errorCode", code.getCode());
        pd.setProperty("timestamp", OffsetDateTime.now().toString());
        pd.setProperty("app", "gateway");

        try {
            pd.setInstance(URI.create(exchange.getRequest().getPath().value()));
        }
        catch (Exception ignore) {
            log.error(code.getI18nKey(), ignore);
        }

        // requestId (iz response headera, pa iz request headera)
        String rid = exchange.getResponse().getHeaders().getFirst(RequestIdFilterConfig.HEADER);
        if (rid == null || rid.isBlank()) {
            rid = exchange.getRequest().getHeaders().getFirst(RequestIdFilterConfig.HEADER);
        }
        if (rid != null && !rid.isBlank()) {
            pd.setProperty("requestId", rid);
        }

        return pd;

    }


    private String defaultTitle(final ErrorCode code) {
        var s = code.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
