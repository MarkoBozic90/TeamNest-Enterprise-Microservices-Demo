package com.teamnest.userservice.model;

import com.teamnest.userservice.model.enums.InviteType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "invite",
    schema = "account_management_service",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_invite_token_hash", columnNames = "token_hash")
        },
        indexes = {
            @Index(name = "idx_invite_email", columnList = "email"),
            @Index(name = "idx_invite_club", columnList = "club_id")
        }
)
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class Invite extends Auditable {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private final UUID id;

    @Column(name = "token_hash", nullable = false, length = 64)
    private final String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 16)
    private final InviteType type; // 'PLAYER' | 'STAFF'

    @Column(name = "club_id", nullable = false)
    private final UUID clubId;

    @Column(name = "expires_at", nullable = false)
    private final Instant expiresAt;

    @Column(name = "redeemed_at")
    private final Instant redeemedAt;

    @Column(name = "redeemed_by")
    private final UUID redeemedBy;

    @Builder.Default
    @Column(name = "max_uses", nullable = false)
    private final Integer maxUses = 1;

    @Builder.Default
    @Column(name = "uses", nullable = false)
    private final Integer uses = 0;
}
