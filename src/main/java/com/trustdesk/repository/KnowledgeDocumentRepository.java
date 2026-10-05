package com.trustdesk.repository;

import com.trustdesk.entity.KnowledgeDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KnowledgeDocumentRepository
        extends JpaRepository<KnowledgeDocument, Long> {

    Optional<KnowledgeDocument> findByKbId(String kbId);
}