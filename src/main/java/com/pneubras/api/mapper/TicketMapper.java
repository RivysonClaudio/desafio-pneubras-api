package com.pneubras.api.mapper;

import com.pneubras.api.dto.response.TicketDetailsResponseDTO;
import com.pneubras.api.dto.response.TicketResumeResponseDTO;
import com.pneubras.api.entity.Ticket;

public class TicketMapper {

    public static TicketDetailsResponseDTO toDetailsDTO(Ticket ticket) {
        return new TicketDetailsResponseDTO(
            ticket.getId(),
            ticket.getTitle(),
            ticket.getDescription(),
            ticket.getPriority(),
            ticket.getStatus(),
            ticket.getDueAt(),
            UserMapper.toDTO(ticket.getCreatedBy()),
            ticket.getCreatedAt(),
            ticket.getUpdatedAt()
        );
    }

    public static TicketResumeResponseDTO toResumeDTO(Ticket ticket) {
        return new TicketResumeResponseDTO(
            ticket.getId(),
            ticket.getTitle(),
            ticket.getPriority(),
            ticket.getStatus(),
            ticket.getDueAt()
        );
    }
}
