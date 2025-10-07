package com.teamnest.userservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
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
import lombok.ToString;

@Entity
@Table(name = "invite_redemptions",
    uniqueConstraints = @UniqueConstraint(name = "uq_invite_user", columnNames = {"invite_id", "user_id"}))
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id", callSuper = false)
@ToString(exclude = "invite")
public class InviteRedemption extends Auditable implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "invite_id", nullable = false)
    @NotNull
    private Invite invite;

    @Column(name = "user_id", nullable = false)
    @NotNull
    private UUID userId;

    @Column(name = "redeemed_at", nullable = false)
    @NotNull
    private Instant redeemedAt;

    @Column(name = "redeemed_by", nullable = false)
    @NotNull
    private UUID redeemedBy;

    public static InviteRedemption of(Invite invite, UUID userId, UUID actorId, Instant at) {
        return InviteRedemption.builder()
            .invite(Objects.requireNonNull(invite, "invite"))
            .userId(Objects.requireNonNull(userId, "userId"))
            .redeemedBy(Objects.requireNonNull(actorId, "redeemedBy"))
            .redeemedAt(Objects.requireNonNullElseGet(at, Instant::now))
            .build();
    }
}