package com.teamnest.userservice.service;



import com.teamnest.userservice.api.request.InviteCreateRequest;
import com.teamnest.userservice.model.Invite;
import com.teamnest.userservice.model.enums.InviteStatus;
import com.teamnest.userservice.model.event.InviteConsumed;
import com.teamnest.userservice.model.event.InviteIssued;
import com.teamnest.userservice.service.impl.InviteServiceImpl;
import java.time.Instant;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public interface InviteService {

    Invite get(UUID inviteId);

    Invite getForUpdate(UUID inviteId);

    Invite save(Invite invite);

    InviteStatus resolveStatus(UUID inviteId, Instant now);

    boolean isExpired(UUID inviteId, Instant now);

    int getRedemptionCount(UUID inviteId);
}