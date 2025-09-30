package com.teamnest.userservice.repository;

import com.teamnest.userservice.model.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("SELECT u FROM User u WHERE u.createdAt " + " <= :targetDate AND :before = true "
        + " OR " + " u.createdAt > :targetDate AND :before = false ")
    List<User> findUsersByCreatedAt(@Param("targetDate") LocalDateTime targetDate, @Param("before") boolean before);

    Optional<User> findByUsername(String username);

}
