package com.teamnest.userservice.model.event;

import com.teamnest.userservice.model.enums.InviteType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Schema(name = "InviteIssued", description = "Event published right after an invite is created.")
public record InviteIssued(
    @Schema(description = "Invite ID", example = "7e1a0f8e-1a0c-4f4a-9f3e-7d6ad6e9b8f1", format = "uuid")
    UUID inviteId,

    @Schema(description = "Club ID that owns the invite", example = "a2a5420b-d8c6-4b06-8e3b-3b6900a5d21f", format = "uuid")
    UUID clubId,

    @Schema(description = "Invite type (domain enum as string)", example = "PLAYER")
    String type,

    @Schema(description = "UTC expiration timestamp", example = "2025-10-01T12:00:00Z")
    Instant expiresAt,

    @Schema(description = "UTC timestamp when the event occurred", example = "2025-09-26T21:39:02Z")
    Instant occurredAt
) implements DomainEvent {

    public String eventType() {
        return "InviteIssued";
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
