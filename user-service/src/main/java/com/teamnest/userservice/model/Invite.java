package com.teamnest.userservice.model;

import com.teamnest.userservice.exception.InviteExhaustedException;
import com.teamnest.userservice.exception.InviteExpiredException;
import com.teamnest.userservice.exception.InviteInvalidStateException;
import com.teamnest.userservice.exception.InviteRevokedException;
import com.teamnest.userservice.model.enums.InviteStatus;
import com.teamnest.userservice.model.enums.InviteType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(
    name = "invite",
    schema = "account_management_service",
    uniqueConstraints = @UniqueConstraint(name = "uk_invite_token_hash", columnNames = "token_hash"),
    indexes = {
        @Index(name = "idx_invite_email", columnList = "email"),
        @Index(name = "idx_invite_club", columnList = "club_id"),
        @Index(name = "idx_invite_expires_at", columnList = "expires_at")
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

    @Version
    private long version;

    @Column(name = "token_hash", nullable = false, length = 64)
    private final String tokenHash;

    @Column(name = "token_salt", length = 32, updatable = false)
    private final String tokenSalt;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 16)
    private final InviteType type; // PLAYER | STAFF | ...

    @Column(name = "club_id", nullable = false)
    private final UUID clubId;

    @Column(name = "email", length = 255) // ako je deo modela
    private final String email;

    @Column(name = "expires_at", nullable = false)
    private final Instant expiresAt;

    @OneToMany(mappedBy = "invite", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<InviteRevocation> revocations = new HashSet<>();

    @OneToMany(mappedBy = "invite", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<InviteRedemption> redemptions = new HashSet<>();

    @Builder.Default
    @Column(name = "usage_limit", nullable = false)
    private final Integer usageLimit = 1;

    @Builder.Default
    @Column(name = "redemption_count", nullable = false)
    private Integer redemptionCount = 0;

    // ---------- Status/guards ----------
    public boolean isExpired(Instant now) {
        final Instant t = (now != null) ? now : Instant.now();
        if (expiresAt == null) {
            throw new InviteInvalidStateException("expiresAt cannot be null");
        }

        return !t.isBefore(expiresAt);
    }

    public boolean isExhausted() {
        return redemptionCount >= usageLimit;
    }

    public boolean isRevoked() {
        return revocations != null && !revocations.isEmpty();
    }

    public InviteStatus getStatus(Instant now) {
        if (isRevoked()) {
            return InviteStatus.REVOKED;
        }
        if (isExpired(now)) {
            return InviteStatus.EXPIRED;
        }
        if (isExhausted()) {
            return InviteStatus.EXHAUSTED;
        }
        return InviteStatus.ACTIVE;
    }

    public void requireActive(Instant now) {
        if (isRevoked()) {
            throw new InviteRevokedException("Invite is revoked");
        }
        if (isExpired(now)) {
            throw new InviteExpiredException("Invite is expired");
        }
        if (isExhausted()) {
            throw new InviteExhaustedException("Invite usage limit reached");
        }
    }

    // ---------- Helpers ----------
    public InviteRedemption addRedemption(UUID userId, UUID actorId, Instant when) {
        requireActive(when);
        var r = InviteRedemption.of(this, userId, actorId, when);
        redemptions.add(r);
        this.redemptionCount = this.redemptionCount + 1;
        return r;
    }

    public InviteRevocation revoke(UUID actorId, String reason, Instant when) {
        var rev = InviteRevocation.of(this, actorId, reason, when);
        revocations.add(rev);
        return rev;
    }

    // ---------- Invariants ----------
    @PrePersist
    @PreUpdate
    private void validateInvariants() {
        if (tokenHash == null || tokenHash.length() != 64) {
            throw new InviteInvalidStateException("tokenHash must be a 64-char hex string");
        }
        if (clubId == null) {
            throw new InviteInvalidStateException("clubId must be provided");
        }
        if (type == null) {
            throw new InviteInvalidStateException("type must be provided");
        }
        if (usageLimit == null || usageLimit < 1) {
            throw new InviteInvalidStateException("usageLimit must be >= 1");
        }
        if (redemptionCount == null || redemptionCount < 0) {
            throw new InviteInvalidStateException("redemptionCount must be >= 0");
        }
    }


    // Convenience factory methods for readability at call sites
    public static Invite singleUse(final UUID clubId,
                                   final InviteType type,
                                   final String tokenHash,
                                   final String tokenSalt,
                                   final Instant expiresAt) {
        return Invite.builder()
            .clubId(clubId)
            .type(type)
            .tokenHash(tokenHash)
            .tokenSalt(tokenSalt)
            .expiresAt(expiresAt)
            .usageLimit(1)
            .redemptionCount(0)
            .build();
    }

    public static Invite multiUse(final UUID clubId,
                                  final InviteType type,
                                  final String tokenHash,
                                  final String tokenSalt,
                                  final Instant expiresAt,
                                  final int usageLimit) {
        return Invite.builder()
            .clubId(clubId)
            .type(type)
            .tokenHash(tokenHash)
            .tokenSalt(tokenSalt)
            .expiresAt(expiresAt)
            .usageLimit(Math.max(usageLimit, 1))
            .redemptionCount(0)
            .build();
    }
}

