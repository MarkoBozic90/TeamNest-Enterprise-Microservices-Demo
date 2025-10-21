package com.teamnest.userservice.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Client locale for i18n message resolution (BCP 47).", example = "en-US")
public record RedeemInviteRequest(
    @Schema(description = "Client locale for i18n message resolution (BCP 47).", example = "en-US")
    UUID userId,
    @Schema(description = "Client locale for i18n message resolution (BCP 47).", example = "en-US")
    UUID actorId,
    @Schema(description = "Client locale for i18n message resolution (BCP 47).", example = "en-US")
    Instant at
) {

}


