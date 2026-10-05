package com.trustdesk.controller;

import com.trustdesk.entity.KnowledgeDocument;
import com.trustdesk.service.KnowledgeBaseService;
import com.trustdesk.service.KnowledgeDocumentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeDocumentController {

    private final KnowledgeDocumentService service;
    private final KnowledgeBaseService knowledgeBaseService;

    public KnowledgeDocumentController(
            KnowledgeDocumentService service,
            KnowledgeBaseService knowledgeBaseService) {

        this.service = service;
        this.knowledgeBaseService = knowledgeBaseService;
    }

    @PostMapping
    public ResponseEntity<KnowledgeDocument> create(
            @RequestBody KnowledgeDocument document) {

        return ResponseEntity.ok(service.create(document));
    }

    @GetMapping("/search")
    public ResponseEntity<List<KnowledgeDocument>> search(
            @RequestParam String query) {

        return ResponseEntity.ok(
                knowledgeBaseService.search(query)
        );
    }
}