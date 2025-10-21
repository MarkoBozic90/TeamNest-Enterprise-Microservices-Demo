package com.teamnest.userservice.model.event;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "outbox_events", schema = "account_management_service",
    indexes = @Index(name = "idx_outbox_type_created", columnList = "type, created_at"))
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 64)
    private String type; // INVITE_REDEEMED, INVITE_REVOKED

    @Column(name = "aggregate_type", nullable = false, length = 64)
    private String aggregateType; // INVITE

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Lob
    @Column(nullable = false)
    private String payloadJson;
}