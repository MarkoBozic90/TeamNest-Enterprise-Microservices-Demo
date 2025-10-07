package com.teamnest.userservice.repository;

import com.teamnest.userservice.model.InviteRedemption;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InviteRedemptionRepository extends JpaRepository<InviteRedemption, UUID> {
}
