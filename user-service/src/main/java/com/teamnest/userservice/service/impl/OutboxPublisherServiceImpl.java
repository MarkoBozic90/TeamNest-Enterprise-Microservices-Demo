package com.teamnest.userservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamnest.userservice.exception.InviteInvalidStateException;
import com.teamnest.userservice.model.InviteRedemption;
import com.teamnest.userservice.model.InviteRevocation;
import com.teamnest.userservice.model.event.OutboxEvent;
import com.teamnest.userservice.repository.OutboxEventRepository;
import com.teamnest.userservice.service.OutboxPublisherService;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// com.teamnest.userservice.service.impl.OutboxPublisherServiceImpl
@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisherServiceImpl implements OutboxPublisherService {

    private final OutboxEventRepository outboxRepo;
    private final ObjectMapper mapper;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.messaging.exchanges.invite:invite.events}")
    private String exchange;

    @Value("${app.messaging.routing.invite.redeemed:invite.redeemed}")
    private String routingRedeemed;

    @Value("${app.messaging.routing.invite.revoked:invite.revoked}")
    private String routingRevoked;

    @Override
    @Transactional
    public void publishInviteRedeemed(InviteRedemption r) {
        var payload = Map.of(
            "eventType", "INVITE_REDEEMED",
            "inviteId", r.getInvite().getId(),
            "userId", r.getUserId(),
            "redeemedBy", r.getRedeemedBy(),
            "redeemedAt", r.getRedeemedAt()
        );
        persistAndSend("INVITE_REDEEMED", r.getInvite().getId(), payload, routingRedeemed);
    }

    @Override
    @Transactional
    public void publishInviteRevoked(InviteRevocation rv) {
        var payload = Map.of(
            "eventType", "INVITE_REVOKED",
            "inviteId", rv.getInvite().getId(),
            "revokedBy", rv.getRevokedBy(),
            "reason", rv.getReason(),
            "revokedAt", rv.getRevokedAt()
        );
        persistAndSend("INVITE_REVOKED", rv.getInvite().getId(), payload, routingRevoked);
    }

    private void persistAndSend(String type, UUID aggregateId, Map<String, ? extends Serializable> payload, String routingKey) {
        try {
            String json = mapper.writeValueAsString(payload);
            var e = OutboxEvent.builder()
                .type(type).aggregateType("INVITE").aggregateId(aggregateId)
                .createdAt(Instant.now()).payloadJson(json)
                .build();
            outboxRepo.save(e);
            rabbitTemplate.convertAndSend(exchange, routingKey, json);
        } catch (Exception ex) {
            log.error("Outbox/Rabbit publish failed", ex);
            throw new InviteInvalidStateException("Outbox publish failed: " + ex.getMessage());
        }
    }
}
