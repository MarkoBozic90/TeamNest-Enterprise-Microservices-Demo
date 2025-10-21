package com.teamnest.userservice.repository;

import com.teamnest.userservice.model.event.OutboxEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

}

