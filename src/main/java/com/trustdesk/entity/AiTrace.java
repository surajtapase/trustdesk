package com.trustdesk.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_traces")
public class AiTrace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ticketId;

    @Column(nullable = false)
    private String runType;

    @Column(columnDefinition = "TEXT")
    private String retrievedDocumentIds;

    @Column(columnDefinition = "TEXT")
    private String toolActions;

    private String guardrailResult;

    private String finalStatus;

    private LocalDateTime createdAt;

    public AiTrace() {
    }

    public Long getId() {
        return id;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getRunType() {
        return runType;
    }

    public void setRunType(String runType) {
        this.runType = runType;
    }

    public String getRetrievedDocumentIds() {
        return retrievedDocumentIds;
    }

    public void setRetrievedDocumentIds(String retrievedDocumentIds) {
        this.retrievedDocumentIds = retrievedDocumentIds;
    }

    public String getToolActions() {
        return toolActions;
    }

    public void setToolActions(String toolActions) {
        this.toolActions = toolActions;
    }

    public String getGuardrailResult() {
        return guardrailResult;
    }

    public void setGuardrailResult(String guardrailResult) {
        this.guardrailResult = guardrailResult;
    }

    public String getFinalStatus() {
        return finalStatus;
    }

    public void setFinalStatus(String finalStatus) {
        this.finalStatus = finalStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}