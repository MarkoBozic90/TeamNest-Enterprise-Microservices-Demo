package com.teamnest.userservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamnest.userservice.exception.InviteInvalidStateException;
import com.teamnest.userservice.model.InviteRedemption;
import com.teamnest.userservice.model.InviteRevocation;
import com.teamnest.userservice.model.event.OutboxEvent;
import com.teamnest.userservice.repository.OutboxEventRepository;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InviteOutboxServiceImpl {

    private final ObjectMapper mapper;
    private final OutboxEventRepository outboxRepo;

    public void publishRedemption(InviteRedemption r) {
        write("INVITE_REDEEMED", "INVITE", r.getInvite().getId(), Map.of(
            "inviteId", r.getInvite().getId(), "userId", r.getUserId(), "redeemedBy", r.getRedeemedBy(), "redeemedAt", r.getRedeemedAt()
        ));
    }
    public void publishRevocation(InviteRevocation rv) {
        write("INVITE_REVOKED", "INVITE", rv.getInvite().getId(), Map.of(
            "inviteId", rv.getInvite().getId(), "revokedBy", rv.getRevokedBy(), "reason", rv.getReason(), "revokedAt", rv.getRevokedAt()
        ));
    }
    private void write(String type, String aggType, UUID aggId, Map<String, Object> payload) {
        try {
            outboxRepo.save(OutboxEvent.builder()
                .type(type).aggregateType(aggType).aggregateId(aggId)
                .createdAt(Instant.now())
                .payloadJson(mapper.writeValueAsString(payload))
                .build());
        } catch (Exception e) {
            throw new InviteInvalidStateException("Outbox write failed: " + e.getMessage());
        }
    }
}
