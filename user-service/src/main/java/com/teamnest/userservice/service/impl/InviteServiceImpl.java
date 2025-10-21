package com.teamnest.userservice.service.impl;

import com.teamnest.userservice.exception.InviteInvalidStateException;
import com.teamnest.userservice.model.Invite;
import com.teamnest.userservice.model.enums.InviteStatus;
import com.teamnest.userservice.repository.InviteRepository;
import com.teamnest.userservice.service.InviteService;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InviteServiceImpl implements InviteService {

    private final InviteRepository repo;

    @Override
    public Invite get(UUID inviteId) {
        return repo.findById(inviteId).orElseThrow(() ->
            new InviteInvalidStateException("Invite not found: " + inviteId));
    }

    @Override
    public Invite getForUpdate(UUID inviteId) {
        Invite i = repo.findByTokenHashForUpdate(inviteId);
        if (i == null) {
            throw new InviteInvalidStateException("Invite not found: " + inviteId);
        }
        return i;
    }

    @Override
    public Invite save(Invite invite) {
        return repo.save(invite);
    }

    @Override
    public InviteStatus resolveStatus(UUID inviteId, Instant now) {
        var i = get(inviteId);
        return i.getStatus(now);
    }

    @Override
    public boolean isExpired(final UUID inviteId, final Instant now) {
        return repo.existsByIdAndExpiresAtLessThanEqual(inviteId, now);
    }

    @Override
    public int getRedemptionCount(final UUID inviteId) {
        Integer c = repo.getRedemptionCountById(inviteId);
        return (c != null) ? c : 0;
    }

}