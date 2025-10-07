package com.teamnest.userservice.model;

import com.teamnest.userservice.exception.InviteExhaustedException;
import com.teamnest.userservice.exception.InviteExpiredException;
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
import org.hibernate.boot.model.naming.IllegalIdentifierException;

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

    @Version
    private long version;

    @Column(name = "token_hash", nullable = false, length = 64)
    private final String tokenHash;

    @Column(name = "token_salt", length = 32, updatable = false)
    private final String tokenSalt;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 16)
    private final InviteType type; // 'PLAYER' | 'STAFF'

    @Column(name = "club_id", nullable = false)
    private final UUID clubId;

    @Column(name = "expires_at", nullable = false)
    private final Instant expiresAt;

    @OneToMany(mappedBy = "invite", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<InviteRevocation> revocations = new HashSet<>();

    @OneToMany(mappedBy = "invite", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<InviteRedemption> redemptions = new HashSet<>();

    @Builder.Default
    @Column(name = "usage_limit ", nullable = false)
    private final Integer usageLimit  = 1;

    @Builder.Default
    @Column(name = "redemption_count", nullable = false)
    private final Integer redemptionCount  = 0;


    /** True if expired by time boundary. */
    public boolean isExpired(Instant now) {
        final Instant t = (now != null) ? now : Instant.now();
        if (expiresAt == null) {
            throw new IllegalIdentifierException("Expires at cannot be null");
        }
        return !t.isBefore(expiresAt);
    }

    /** True if usage limit reached or exceeded. */
    public boolean isExhausted() {
        return redemptionCount >= usageLimit;
    }

    /** True if there exists any active revocation. Adjust if your model has "active" flag. */
    public boolean isRevoked() {
        return revocations != null && !revocations.isEmpty();
    }

    /** Current high-level status (single source of truth for clients). */
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

    /** Can this invite be redeemed right now? */
    public boolean canBeRedeemed(Instant now) {
        return getStatus(now) == InviteStatus.ACTIVE;
    }

    /**
     * Record a successful redemption (side-effecting).
     * Must be called within a @Transactional service that persists this aggregate,
     * relying on @Version for optimistic concurrency control.
     */

    public void onRedeemed(UUID userId, Instant when) {
        // Defensive checks (service should have validated, but keep aggregate safe)
        if (!canBeRedeemed(when)) {
            throw new IllegalStateException("Invite cannot be redeemed in its current state: " + getStatus(when));
        }

        // Increment counter atomically in aggregate invariants.
        this.redemptionCount = this.redemptionCount + 1;

    }



    // =================================================================
    // Invariants & factories
    // =================================================================

    /** Enforce core invariants at creation time. Keep entity always-valid. */
    @PrePersist
    @PreUpdate
    private void validateInvariants() {

        if (tokenHash == null || tokenHash.length() != 64) {
            throw new IllegalStateException("tokenHash must be a 64-char hex string");
        }
        if (clubId == null) {
            throw new IllegalStateException("clubId must be provided");
        }
        if (type == null) {
            throw new IllegalStateException("type must be provided");
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

