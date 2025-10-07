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
@Table(
    name = "invite_revocations",
    indexes = {
        @Index(name = "idx_revocations_invite_at", columnList = "invite_id, revoked_at")
    }
)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode(of = "id", callSuper = true)
@ToString(exclude = "invite")
public class InviteRevocation extends  Auditable implements Serializable {

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

    @Column(name = "revoked_by", nullable = false)
    @NotNull
    private UUID revokedBy;

    @Column(name = "revoked_at", nullable = false)
    @NotNull
    private Instant revokedAt;

    @Column(name = "reason", nullable = false, length = 255)
    @NotNull
    private String reason;

    public static InviteRevocation of(Invite invite, UUID revokedBy, String reason, Instant at) {
        return InviteRevocation.builder()
            .invite(Objects.requireNonNull(invite, "invite"))
            .revokedBy(Objects.requireNonNull(revokedBy, "revokedBy"))
            .reason(Objects.requireNonNull(reason, "reason"))
            .revokedAt(Objects.requireNonNullElseGet(at, Instant::now))
            .build();
    }
}
