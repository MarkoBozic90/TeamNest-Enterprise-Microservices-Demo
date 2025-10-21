package com.teamnest.userservice.model.event;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(name = "InviteRevoked", description = "Event published when an invite gets revoked.")
public record InviteRevoked(
    @Schema(description = "Invite ID", example = "7e1a0f8e-1a0c-4f4a-9f3e-7d6ad6e9b8f1", format = "uuid")
    UUID inviteId,

    @Schema(description = "Actor that performed revocation (user/email/system)", example = "admin@teamnest.io")
    String revokedBy,

    @Schema(description = "UTC revocation timestamp", example = "2025-09-27T10:00:00Z")
    Instant revokedAt,

    @Schema(description = "Optional reason for revocation", example = "User reported abuse")
    String reason,

    @Schema(description = "UTC timestamp when the event occurred", example = "2025-09-27T10:00:00Z")
    Instant occurredAt
) implements DomainEvent {
    public String eventType() {
        return "InviteRevoked";
    }

    public String version() {
        return "v1";
    }

    public Instant occurredAt() {
        return occurredAt;
    }

    public UUID aggregateId() {
        return inviteId;
    }
}
