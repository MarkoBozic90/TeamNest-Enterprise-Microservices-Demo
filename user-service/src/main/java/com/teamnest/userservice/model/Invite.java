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


    public void assertRedeemable(Instant now) {
        assert expiresAt != null;
        if (expiresAt.isBefore(now)) {
            throw new InviteExpiredException(id);
        }
        if (uses >= maxUses) {
            throw new InviteExhaustedException(id);
        }
        if (!revocations.isEmpty()) {
            throw new IllegalStateException("Invite revoked");
        }
    }

    public void incrementUse() {
        this.uses = this.uses + 1;
    }

    public InviteRedemption addRedemption(UUID userId, Instant at) {
        InviteRedemption r = InviteRedemption.builder()
            .invite(this)
            .redeemedBy(userId)
            .redeemedAt(at)
            .build();
        this.redemptions.add(r);
        return r;
    }

    public InviteRevocation revoke(String actor, Instant at, String reason) {
        InviteRevocation rev = InviteRevocation.builder()
            .invite(this)
            .revokedBy(actor)
            .revokedAt(at)
            .reason(reason)
            .build();
        this.revocations.add(rev);
        return rev;
    }
}

