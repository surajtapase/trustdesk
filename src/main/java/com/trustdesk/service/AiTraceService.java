package com.trustdesk.service;

import com.trustdesk.entity.AiTrace;
import com.trustdesk.repository.AiTraceRepository;
import org.springframework.stereotype.Service;

@Service
public class AiTraceService {

    private final AiTraceRepository aiTraceRepository;

    public AiTraceService(
            AiTraceRepository aiTraceRepository) {
        this.aiTraceRepository = aiTraceRepository;
    }

    public AiTrace recordTrace(
            String ticketId,
            String runType,
            String retrievedDocumentIds,
            String toolActions,
            String guardrailResult,
            String finalStatus) {

        AiTrace trace = new AiTrace();

        trace.setTicketId(ticketId);
        trace.setRunType(runType);
        trace.setRetrievedDocumentIds(retrievedDocumentIds);
        trace.setToolActions(toolActions);
        trace.setGuardrailResult(guardrailResult);
        trace.setFinalStatus(finalStatus);

        return aiTraceRepository.save(trace);
    }

    public java.util.List<AiTrace> getTicketTraces(
            String ticketId) {

        return aiTraceRepository
                .findByTicketIdOrderByCreatedAtDesc(ticketId);
    }
}