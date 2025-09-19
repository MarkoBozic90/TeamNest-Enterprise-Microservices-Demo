package com.teamnest.gateway;

import io.swagger.v3.oas.annotations.media.Schema;
import java.net.URI;
import java.util.Locale;
import org.springframework.web.server.ServerWebExchange;


@Schema(
    name = "FallbackProblemParams",
    description = "Parameters used to build an RFC 7807 ProblemDetail when a circuit/gateway fallback is triggered."
)
public record FallbackProblemParams(

    @Schema(
        description = "Client locale for i18n message resolution (BCP 47).",
        example = "en-US"
    )
    Locale locale,

    @Schema(
        description = "Reactive server exchange (used to derive path, "
            + "headers, request-id). Hidden from external OpenAPI.",
        hidden = true,
        accessMode = Schema.AccessMode.READ_ONLY
    )
    ServerWebExchange exchange,

    @Schema(
        description = "Logical upstream/service name (routeId, circuit name, or service alias).",
        example = "users"
    )
    String serviceName,

    @Schema(
        description = "Message bundle key for the Problem 'title'.",
        example = "error.upstream.unavailable"
    )
    String titleKey,

    @Schema(
        description = "Default title if i18n lookup fails.",
        example = "Upstream service unavailable"
    )
    String titleDefault,

    @Schema(
        description = "Message bundle key for the Problem 'detail'.",
        example = "error.users.fallback"
    )
    String detailKey,

    @Schema(
        description = "Default detail if i18n lookup fails.",
        example = "Users service is temporarily unavailable. Fallback executed."
    )
    String detailDefault,

    @Schema(
        description = "Problem type URI (RFC 7807). Should be absolute and stable.",
        example = "https://api.example.com/problems/upstream-unavailable",
        format = "uri"
    )
    URI type
) {

}