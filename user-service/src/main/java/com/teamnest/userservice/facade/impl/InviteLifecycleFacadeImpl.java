package com.teamnest.userservice.facade.impl;

import com.teamnest.userservice.api.respnse.InviteStatusSummary;
import com.teamnest.userservice.exception.InviteInvalidStateException;
import com.teamnest.userservice.facade.InviteLifecycleFacade;
import com.teamnest.userservice.model.Invite;
import com.teamnest.userservice.model.InviteRedemption;
import com.teamnest.userservice.model.InviteRevocation;
import com.teamnest.userservice.service.InviteRedemptionService;
import com.teamnest.userservice.service.InviteRevocationService;
import com.teamnest.userservice.service.InviteService;
import com.teamnest.userservice.service.OutboxPublisherService;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InviteLifecycleFacadeImpl implements InviteLifecycleFacade {

    private final InviteService inviteService;
    private final InviteRedemptionService redemptionService;
    private final InviteRevocationService revocationService;
    private final OutboxPublisherService outbox;

    @Transactional
    @Override
    public InviteRedemption redeem(UUID inviteId, UUID userId, UUID actorId, Instant when) {
        if (redemptionService.existsForUser(inviteId, userId)) {
            throw new InviteInvalidStateException("Already redeemed for user: " + userId);
        }
        Invite invite = inviteService.getForUpdate(inviteId);

        InviteRedemption redemption = invite.addRedemption(userId, actorId, when);
        inviteService.save(invite);
        InviteRedemption saved = redemptionService.save(redemption);
        outbox.publishInviteRedeemed(saved);
        return saved;
    }

    @Transactional
    @Override
    public InviteRevocation revoke(UUID inviteId, UUID actorId, String reason, Instant when) {
        Invite invite = inviteService.getForUpdate(inviteId);
        InviteRevocation rev = invite.revoke(actorId, reason, when);
        inviteService.save(invite);
        InviteRevocation saved = revocationService.save(rev);
        outbox.publishInviteRevoked(saved);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public InviteStatusSummary getStatusSummary(UUID inviteId, Instant now) {
        var invite = inviteService.get(inviteId);
        var status = invite.getStatus(now);
        var revoked = revocationService.existsAnyByInviteId(inviteId);
        var rc = inviteService.getRedemptionCount(inviteId);
        return new InviteStatusSummary(inviteId, status.name(), revoked, rc, invite.getUsageLimit());
    }
}
