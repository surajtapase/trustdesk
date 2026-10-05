package com.trustdesk.dto;

import com.trustdesk.entity.Customer;
import com.trustdesk.entity.Order;
import com.trustdesk.entity.Ticket;

public class TicketContextResponse {

    private Ticket ticket;
    private Customer customer;
    private Order order;

    public TicketContextResponse(
            Ticket ticket,
            Customer customer,
            Order order) {
        this.ticket = ticket;
        this.customer = customer;
        this.order = order;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Order getOrder() {
        return order;
    }
}