package com.teamnest.userservice.service.impl;


import com.teamnest.userservice.model.InviteRevocation;
import com.teamnest.userservice.repository.InviteRevocationRepository;
import com.teamnest.userservice.service.InviteRevocationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InviteRevocationServiceImpl implements InviteRevocationService {

    private final InviteRevocationRepository repo;

    @Override
    public InviteRevocation save(InviteRevocation revocation) {
        return repo.save(revocation);
    }

    @Override
    public List<InviteRevocation> findAllByInviteId(final UUID inviteId) {
        return repo.findAllByInviteId(inviteId);
    }

    @Override
    public boolean existsAnyByInviteId(final UUID inviteId) {
        return repo.existsByInviteId(inviteId);
    }
}