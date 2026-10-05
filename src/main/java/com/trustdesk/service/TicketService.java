package com.trustdesk.service;
import com.trustdesk.dto.TicketContextResponse;
import com.trustdesk.entity.Customer;
import com.trustdesk.entity.KnowledgeDocument;
import com.trustdesk.entity.Order;
import com.trustdesk.entity.Ticket;
import com.trustdesk.repository.CustomerRepository;
import com.trustdesk.repository.OrderRepository;
import com.trustdesk.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final KnowledgeBaseService knowledgeBaseService;

    public TicketService(
            TicketRepository ticketRepository,
            CustomerRepository customerRepository,
            OrderRepository orderRepository,
            KnowledgeBaseService knowledgeBaseService) {

        this.ticketRepository = ticketRepository;
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.knowledgeBaseService = knowledgeBaseService;
    }

    public Ticket createTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public Ticket getTicketByTicketId(String ticketId) {
        return ticketRepository.findByTicketId(ticketId)
                .orElseThrow(() ->
                        new RuntimeException("Ticket not found: " + ticketId));
    }

    public TicketContextResponse getTicketContext(String ticketId) {

        Ticket ticket = getTicketByTicketId(ticketId);

        Customer customer = customerRepository
                .findByCustomerId(ticket.getCustomerId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Customer not found: " + ticket.getCustomerId()));

        Order order = orderRepository
                .findByOrderId(ticket.getOrderId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Order not found: " + ticket.getOrderId()));

        return new TicketContextResponse(ticket, customer, order);
    }

    public List<KnowledgeDocument> getKnowledgeForTicket(String ticketId) {

        Ticket ticket = getTicketByTicketId(ticketId);

        String query =
                ticket.getSubject() + " " +
                        ticket.getBody();

        return knowledgeBaseService.search(query);
    }
}