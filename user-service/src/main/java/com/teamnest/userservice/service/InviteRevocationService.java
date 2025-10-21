package com.teamnest.userservice.service;

import com.teamnest.userservice.model.InviteRevocation;
import java.util.List;
import java.util.UUID;

public interface InviteRevocationService {
    InviteRevocation save(InviteRevocation revocation);

    List<InviteRevocation> findAllByInviteId(UUID inviteId);

    boolean existsAnyByInviteId(UUID inviteId);
}