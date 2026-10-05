package com.trustdesk.controller;

import com.trustdesk.entity.AiTrace;
import com.trustdesk.service.AiTraceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/traces")
public class AiTraceController {

    private final AiTraceService aiTraceService;

    public AiTraceController(
            AiTraceService aiTraceService) {

        this.aiTraceService = aiTraceService;
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<List<AiTrace>> getTicketTraces(
            @PathVariable String ticketId) {

        return ResponseEntity.ok(
                aiTraceService.getTicketTraces(ticketId)
        );
    }
}