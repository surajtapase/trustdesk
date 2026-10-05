package com.trustdesk.controller;

import com.trustdesk.entity.ToolAction;
import com.trustdesk.service.ToolActionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/actions")
public class ToolActionController {

    private final ToolActionService toolActionService;

    public ToolActionController(
            ToolActionService toolActionService) {

        this.toolActionService = toolActionService;
    }

    @PostMapping("/refund-review/{ticketId}")
    public ResponseEntity<ToolAction> requestRefundReview(
            @PathVariable String ticketId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestParam(
                    defaultValue = "support-agent"
            ) String requestedBy) {

        return ResponseEntity.ok(
                toolActionService.requestRefundReview(
                        ticketId,
                        idempotencyKey,
                        requestedBy
                )
        );
    }

    @PostMapping("/{actionId}/approve")
    public ResponseEntity<ToolAction> approveAction(
            @PathVariable Long actionId) {

        return ResponseEntity.ok(
                toolActionService.approveAction(actionId)
        );
    }

    @PostMapping("/{actionId}/execute")
    public ResponseEntity<ToolAction> executeAction(
            @PathVariable Long actionId) {

        return ResponseEntity.ok(
                toolActionService.executeAction(actionId)
        );
    }

    @GetMapping("/{actionId}")
    public ResponseEntity<ToolAction> getAction(
            @PathVariable Long actionId) {

        return ResponseEntity.ok(
                toolActionService.getAction(actionId)
        );
    }
}