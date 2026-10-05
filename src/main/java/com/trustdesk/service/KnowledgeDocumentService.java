package com.trustdesk.service;

import com.trustdesk.entity.KnowledgeDocument;
import com.trustdesk.repository.KnowledgeDocumentRepository;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeDocumentService {

    private final KnowledgeDocumentRepository repository;

    public KnowledgeDocumentService(KnowledgeDocumentRepository repository) {
        this.repository = repository;
    }

    public KnowledgeDocument create(KnowledgeDocument document) {
        return repository.save(document);
    }
}