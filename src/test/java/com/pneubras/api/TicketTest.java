package com.pneubras.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.pneubras.api.entity.Ticket;
import com.pneubras.api.entity.TicketStatus;
import com.pneubras.api.exception.custom.InvalidStatusTransitionException;

public class TicketTest {

    @Test
    void shouldAllowValidTransition() {
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.OPEN);

        ticket.updateStatus(TicketStatus.IN_PROGRESS);

        assertEquals(TicketStatus.IN_PROGRESS, ticket.getStatus());
    }

    @Test
    void shouldNotAllowInvalidTransition() {
        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.OPEN);

        Exception exception = assertThrows(
            InvalidStatusTransitionException.class,
            () -> ticket.updateStatus(TicketStatus.RESOLVED)
        );

        assertTrue(exception.getMessage().contains("Cannot change status"));
    }
}
