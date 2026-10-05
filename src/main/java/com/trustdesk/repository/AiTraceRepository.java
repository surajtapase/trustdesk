package com.trustdesk.repository;

import com.trustdesk.entity.AiTrace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiTraceRepository
        extends JpaRepository<AiTrace, Long> {

    List<AiTrace> findByTicketIdOrderByCreatedAtDesc(
            String ticketId
    );
}