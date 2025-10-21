package com.teamnest.userservice.service;

import com.teamnest.userservice.model.InviteRedemption;
import com.teamnest.userservice.model.InviteRevocation;

public interface OutboxPublisherService {
    void publishInviteRedeemed(InviteRedemption redemption);
    void publishInviteRevoked(InviteRevocation revocation);
}