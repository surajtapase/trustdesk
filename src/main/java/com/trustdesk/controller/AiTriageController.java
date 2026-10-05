package com.trustdesk.controller;

import com.trustdesk.dto.AiSupportResponse;
import com.trustdesk.dto.AiTriageResponse;
import com.trustdesk.service.AiSupportService;
import com.trustdesk.service.AiTriageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiTriageController {

    private final AiTriageService aiTriageService;
    private final AiSupportService aiSupportService;

    public AiTriageController(
            AiTriageService aiTriageService,
            AiSupportService aiSupportService) {

        this.aiTriageService = aiTriageService;
        this.aiSupportService = aiSupportService;
    }

    @PostMapping("/triage/{ticketId}")
    public ResponseEntity<AiTriageResponse> triageTicket(
            @PathVariable String ticketId) {

        return ResponseEntity.ok(
                aiTriageService.triageTicket(ticketId)
        );
    }

    @PostMapping("/support/{ticketId}")
    public ResponseEntity<AiSupportResponse> generateSupportResponse(
            @PathVariable String ticketId) {

        return ResponseEntity.ok(
                aiSupportService.generateSupportResponse(ticketId)
        );
    }
}
