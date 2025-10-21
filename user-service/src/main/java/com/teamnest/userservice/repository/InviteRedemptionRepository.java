package com.teamnest.userservice.repository;

import com.teamnest.userservice.model.Invite;
import com.teamnest.userservice.model.InviteRedemption;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface InviteRedemptionRepository extends JpaRepository<InviteRedemption, UUID> {

    boolean existsByInviteIdAndUserId(UUID inviteId, UUID userId);

    long countByInviteId(UUID inviteId);

    List<InviteRedemption> findAllByInviteId(UUID inviteId);
}