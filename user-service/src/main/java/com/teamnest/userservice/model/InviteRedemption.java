package com.teamnest.userservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "invite_redemptions",
    uniqueConstraints = @UniqueConstraint(name = "uq_invite_user", columnNames = {"invite_id", "user_id"}),
    indexes = @Index(name = "idx_redemptions_invite_at", columnList = "invite_id, redeemed_at"))
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id", callSuper = false)
public class InviteRedemption extends Auditable implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "invite_id", nullable = false)
    private Invite invite;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "redeemed_at", nullable = false)
    private Instant redeemedAt;

    @Column(name = "redeemed_by", nullable = false)
    private UUID redeemedBy;

    public static InviteRedemption of(Invite invite, UUID userId, UUID actorId, Instant at) {
        return InviteRedemption.builder()
            .invite(Objects.requireNonNull(invite))
            .userId(Objects.requireNonNull(userId))
            .redeemedBy(Objects.requireNonNull(actorId))
            .redeemedAt(Objects.requireNonNullElseGet(at, Instant::now))
            .build();
    }
}