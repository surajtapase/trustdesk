package com.trustdesk.controller;
import com.trustdesk.dto.TicketContextResponse;
import com.trustdesk.entity.KnowledgeDocument;
import com.trustdesk.entity.Ticket;
import com.trustdesk.service.TicketService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<Ticket> createTicket(
            @RequestBody Ticket ticket) {

        Ticket savedTicket = ticketService.createTicket(ticket);

        return ResponseEntity.ok(savedTicket);
    }

    @GetMapping
    public ResponseEntity<List<Ticket>> getAllTickets() {

        return ResponseEntity.ok(
                ticketService.getAllTickets()
        );
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<Ticket> getTicketByTicketId(
            @PathVariable String ticketId) {

        return ResponseEntity.ok(
                ticketService.getTicketByTicketId(ticketId)
        );
    }

    @GetMapping("/{ticketId}/context")
    public ResponseEntity<TicketContextResponse> getTicketContext(
            @PathVariable String ticketId) {

        return ResponseEntity.ok(
                ticketService.getTicketContext(ticketId)
        );
    }

    @GetMapping("/{ticketId}/knowledge")
    public ResponseEntity<List<KnowledgeDocument>> getTicketKnowledge(
            @PathVariable String ticketId) {

        return ResponseEntity.ok(
                ticketService.getKnowledgeForTicket(ticketId)
        );
    }
}
