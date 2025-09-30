package com.teamnest.userservice.repository;

import com.teamnest.userservice.model.Invite;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;



public interface InviteRepository  extends JpaRepository<Invite, UUID> {

    Optional<Invite> findByTokenHash(String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Invite i where i.tokenHash = :hash")
    Optional<Invite> findByTokenHashForUpdate(@Param("hash") String tokenHash);
}
