package com.teamnest.userservice.repository;

import com.teamnest.userservice.model.InviteRevocation;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InviteRevocationRepository extends JpaRepository<InviteRevocation, UUID> {
}
