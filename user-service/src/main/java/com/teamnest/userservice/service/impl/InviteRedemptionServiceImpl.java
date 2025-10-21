package com.teamnest.userservice.service.impl;

import com.teamnest.userservice.model.InviteRedemption;
import com.teamnest.userservice.repository.InviteRedemptionRepository;
import com.teamnest.userservice.service.InviteRedemptionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class InviteRedemptionServiceImpl implements InviteRedemptionService {

    private final InviteRedemptionRepository repo;

    @Override
    public boolean existsForUser(UUID inviteId, UUID userId) {
        return repo.existsByInviteIdAndUserId(inviteId, userId);
    }

    @Override
    public InviteRedemption save(InviteRedemption redemption) {
        return repo.save(redemption);
    }

    @Override
    public long countByInviteId(final UUID inviteId) {
        return repo.countByInviteId(inviteId);
    }

    @Override
    public List<InviteRedemption> findAllByInviteId(final UUID inviteId) {
        return repo.findAllByInviteId(inviteId);
    }
}
