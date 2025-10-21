package com.teamnest.userservice.api.respnse;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Client locale for i18n message resolution (BCP 47).", example = "en-US")
public record InviteStatusSummary(
    @Schema(description = "Client locale for i18n message resolution (BCP 47).", example = "en-US")
    UUID inviteId,
    @Schema(description = "Client locale for i18n message resolution (BCP 47).", example = "en-US")
    String status,
    @Schema(description = "Client locale for i18n message resolution (BCP 47).", example = "en-US")
    boolean revoked,
    @Schema(description = "Client locale for i18n message resolution (BCP 47).", example = "en-US")
    int redemptionCount,
    @Schema(description = "Client locale for i18n message resolution (BCP 47).", example = "en-US")
    int usageLimit
) {

}