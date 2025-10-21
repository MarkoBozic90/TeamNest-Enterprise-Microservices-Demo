package com.teamnest.userservice.service;

import com.teamnest.userservice.model.InviteRedemption;
import java.util.List;
import java.util.UUID;

public interface InviteRedemptionService {

    boolean existsForUser(UUID inviteId, UUID userId);

    InviteRedemption save(InviteRedemption redemption);

    long countByInviteId(UUID inviteId);

    List<InviteRedemption> findAllByInviteId(UUID inviteId);
}