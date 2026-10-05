package com.trustdesk.service;
import com.trustdesk.entity.KnowledgeDocument;
import com.trustdesk.repository.KnowledgeDocumentRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class KnowledgeBaseService {

    private final KnowledgeDocumentRepository knowledgeDocumentRepository;

    public KnowledgeBaseService(
            KnowledgeDocumentRepository knowledgeDocumentRepository) {

        this.knowledgeDocumentRepository = knowledgeDocumentRepository;
    }

    public List<KnowledgeDocument> search(String query) {

        List<KnowledgeDocument> documents =
                knowledgeDocumentRepository.findAll();

        if (query == null || query.isBlank()) {
            return new ArrayList<>();
        }

        String[] keywords = query
                .toLowerCase()
                .replaceAll("[^a-z0-9 ]", " ")
                .split("\\s+");

        List<ScoredDocument> scoredDocuments = new ArrayList<>();

        for (KnowledgeDocument document : documents) {

            String searchableText =
                    (
                            document.getTitle() + " " +
                                    document.getCategory() + " " +
                                    document.getContent()
                    )
                            .toLowerCase();

            int score = 0;

            for (String keyword : keywords) {

                if (keyword.length() < 4) {
                    continue;
                }

                if (searchableText.contains(keyword)) {
                    score++;
                }
            }

            if (score > 0) {
                scoredDocuments.add(
                        new ScoredDocument(document, score)
                );
            }
        }

        scoredDocuments.sort(
                Comparator.comparingInt(
                        ScoredDocument::score
                ).reversed()
        );

        return scoredDocuments.stream()
                .limit(3)
                .map(ScoredDocument::document)
                .toList();
    }

    private record ScoredDocument(
            KnowledgeDocument document,
            int score) {
    }
}