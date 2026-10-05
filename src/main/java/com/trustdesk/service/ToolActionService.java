package com.trustdesk.service;

import com.trustdesk.entity.ToolAction;
import com.trustdesk.repository.ToolActionRepository;
import org.springframework.stereotype.Service;

@Service
public class ToolActionService {

    private static final String START_REFUND_REVIEW =
            "start_refund_review";

    private static final String PENDING_APPROVAL =
            "PENDING_APPROVAL";

    private static final String APPROVED =
            "APPROVED";

    private static final String EXECUTED =
            "EXECUTED";

    private final ToolActionRepository toolActionRepository;
    private final TicketService ticketService;

    public ToolActionService(
            ToolActionRepository toolActionRepository,
            TicketService ticketService) {

        this.toolActionRepository = toolActionRepository;
        this.ticketService = ticketService;
    }

    public ToolAction requestRefundReview(
            String ticketId,
            String idempotencyKey,
            String requestedBy) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Idempotency-Key is required."
            );
        }

        // Validate that the ticket exists
        ticketService.getTicketByTicketId(ticketId);

        // Idempotency check
        var existingAction =
                toolActionRepository
                        .findByIdempotencyKey(idempotencyKey);

        if (existingAction.isPresent()) {
            return existingAction.get();
        }

        ToolAction action = new ToolAction();

        action.setTicketId(ticketId);
        action.setActionType(START_REFUND_REVIEW);
        action.setStatus(PENDING_APPROVAL);
        action.setIdempotencyKey(idempotencyKey);
        action.setRequestedBy(requestedBy);

        return toolActionRepository.save(action);
    }

    public ToolAction approveAction(Long actionId) {

        ToolAction action =
                toolActionRepository.findById(actionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Action not found: " + actionId
                                ));

        if (!PENDING_APPROVAL.equals(action.getStatus())) {
            throw new IllegalStateException(
                    "Action cannot be approved from status: "
                            + action.getStatus()
            );
        }

        action.setStatus(APPROVED);

        return toolActionRepository.save(action);
    }

    public ToolAction executeAction(Long actionId) {

        ToolAction action =
                toolActionRepository.findById(actionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Action not found: " + actionId
                                ));

        if (!APPROVED.equals(action.getStatus())) {
            throw new IllegalStateException(
                    "Action must be APPROVED before execution."
            );
        }

        action.setStatus(EXECUTED);

        return toolActionRepository.save(action);
    }

    public ToolAction getAction(Long actionId) {

        return toolActionRepository.findById(actionId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Action not found: " + actionId
                        ));
    }
}
