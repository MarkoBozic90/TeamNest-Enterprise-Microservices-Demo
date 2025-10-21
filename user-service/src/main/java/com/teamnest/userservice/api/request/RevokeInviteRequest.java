package com.teamnest.userservice.api.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Client locale for i18n message resolution (BCP 47).", example = "en-US")
public record RevokeInviteRequest(
    @Schema(description = "Client locale for i18n message resolution (BCP 47).", example = "en-US")
    UUID actorId,
    @Schema(description = "Client locale for i18n message resolution (BCP 47).", example = "en-US")
    String reason,
    @Schema(description = "Client locale for i18n message resolution (BCP 47).", example = "en-US")
    Instant at
) {

}
