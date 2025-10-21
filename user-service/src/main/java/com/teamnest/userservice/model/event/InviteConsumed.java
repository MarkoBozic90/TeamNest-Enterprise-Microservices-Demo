package com.teamnest.userservice.model.event;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(name = "InviteConsumed", description = "Event published when an invite is redeemed by a user.")
public record InviteConsumed(
    @Schema(description = "Invite ID", example = "7e1a0f8e-1a0c-4f4a-9f3e-7d6ad6e9b8f1", format = "uuid")
    UUID inviteId,

    @Schema(description = "User ID who consumed the invite", example = "f6c2b9b7-1a62-4024-9a1c-ec0d0a3b1e8a", format = "uuid")
    UUID userId,

    @Schema(description = "UTC timestamp when the event occurred", example = "2025-09-26T22:05:42Z")
    Instant occurredAt
) implements DomainEvent {
    public String eventType() {
        return "InviteConsumed";
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