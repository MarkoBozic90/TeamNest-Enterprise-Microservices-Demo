package com.teamnest.userservice.repository;

import com.teamnest.userservice.model.InviteRevocation;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InviteRevocationRepository extends JpaRepository<InviteRevocation, UUID> {

    @Query("select count(r) > 0 from InviteRevocation r where r.invite.id = :inviteId and r.revokedAt <= :at")
    boolean existsRevocationAsOf(UUID inviteId, Instant at);

    boolean existsByInviteId(UUID inviteId);

    List<InviteRevocation> findAllByInviteId(UUID inviteId);

}
