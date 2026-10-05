package com.trustdesk.dto;

import java.util.List;

public class AiSupportResponse {

    private AiTriageResponse triage;
    private String draftResponse;
    private List<String> citations;

    public AiSupportResponse() {
    }

    public AiSupportResponse(
            AiTriageResponse triage,
            String draftResponse,
            List<String> citations) {

        this.triage = triage;
        this.draftResponse = draftResponse;
        this.citations = citations;
    }

    public AiTriageResponse getTriage() {
        return triage;
    }

    public void setTriage(AiTriageResponse triage) {
        this.triage = triage;
    }

    public String getDraftResponse() {
        return draftResponse;
    }

    public void setDraftResponse(String draftResponse) {
        this.draftResponse = draftResponse;
    }

    public List<String> getCitations() {
        return citations;
    }

    public void setCitations(List<String> citations) {
        this.citations = citations;
    }
}
