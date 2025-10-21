package com.teamnest.userservice.facade;

import com.teamnest.userservice.api.respnse.InviteStatusSummary;
import com.teamnest.userservice.model.InviteRedemption;
import com.teamnest.userservice.model.InviteRevocation;
import java.time.Instant;
import java.util.UUID;

public interface InviteLifecycleFacade {
    InviteRedemption redeem(UUID inviteId, UUID userId, UUID actorId, Instant when);
    InviteRevocation revoke(UUID inviteId, UUID actorId, String reason, Instant when);

    InviteStatusSummary getStatusSummary(UUID inviteId, Instant now);
}