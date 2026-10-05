package com.trustdesk.repository;

import com.trustdesk.entity.ToolAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ToolActionRepository
        extends JpaRepository<ToolAction, Long> {

    Optional<ToolAction> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);
}