package com.teamnest.userservice.model.event;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Marker interface for domain events emitted by the aggregate.")
public interface DomainEvent {
    @Schema(description = "Event type name, e.g. InviteIssued", example = "InviteIssued")
    String eventType();

    @Schema(description = "Payload version to support evolution over time", example = "v1")
    String version();

    @Schema(description = "UTC timestamp when the event occurred", example = "2025-09-26T21:39:02Z")
    Instant occurredAt();

    @Schema(description = "Aggregate ID this event belongs to", example = "2f5c47a0-4a43-4f7f-bb63-5a8f9e1f2d4a", format = "uuid")
    UUID aggregateId();
}
