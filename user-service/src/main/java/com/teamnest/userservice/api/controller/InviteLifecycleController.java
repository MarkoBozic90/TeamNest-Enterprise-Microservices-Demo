package com.teamnest.userservice.api.controller;

import com.teamnest.userservice.api.request.RedeemInviteRequest;
import com.teamnest.userservice.api.request.RevokeInviteRequest;
import com.teamnest.userservice.api.respnse.InviteStatusSummary;
import com.teamnest.userservice.facade.InviteLifecycleFacade;
import com.teamnest.userservice.model.InviteRedemption;
import com.teamnest.userservice.model.InviteRevocation;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invites")
@RequiredArgsConstructor
public class InviteLifecycleController {

    private final InviteLifecycleFacade facade;

    @PostMapping("/{id}/redeem")
    @ResponseStatus(HttpStatus.CREATED)
    public InviteRedemption redeem(@PathVariable UUID id, @Valid @RequestBody RedeemInviteRequest req) {
        return facade.redeem(id, req.userId(), req.actorId(), req.at() != null ? req.at() : Instant.now());
    }

    @PostMapping("/{id}/revoke")
    @ResponseStatus(HttpStatus.CREATED)
    public InviteRevocation revoke(@PathVariable UUID id, @Valid @RequestBody RevokeInviteRequest req) {
        return facade.revoke(id, req.actorId(), req.reason(), req.at() != null ? req.at() : Instant.now());
    }

    @GetMapping("/{id}/status")
    public InviteStatusSummary status(@PathVariable UUID id) {
        return facade.getStatusSummary(id, Instant.now());
    }
}