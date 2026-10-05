package com.trustdesk.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "evaluation_cases")
public class EvaluationCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "case_id", nullable = false, unique = true)
    private String caseId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String caseData;

    public EvaluationCase() {
    }

    public Long getId() {
        return id;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getCaseData() {
        return caseData;
    }

    public void setCaseData(String caseData) {
        this.caseData = caseData;
    }
}