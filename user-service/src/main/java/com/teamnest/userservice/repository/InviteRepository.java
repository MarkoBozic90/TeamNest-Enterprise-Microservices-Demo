package com.teamnest.userservice.repository;

import com.teamnest.userservice.model.Invite;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;



public interface InviteRepository  extends JpaRepository<Invite, UUID> {

    Optional<Invite> findByTokenHash(String tokenHash);
    boolean existsByTokenHash(String tokenHash);

    @Query("select i.redemptionCount from Invite i where i.id = :id")
    Integer getRedemptionCountById(UUID id);

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query("select i from Invite i where i.id = :id")
    Invite findByTokenHashForUpdate(UUID id);

    boolean existsByIdAndExpiresAtLessThanEqual(UUID id, Instant now);}
