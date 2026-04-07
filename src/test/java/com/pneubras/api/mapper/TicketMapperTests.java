package com.pneubras.api.mapper;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import com.pneubras.api.dto.response.TicketDetailsResponseDTO;
import com.pneubras.api.dto.response.TicketResumeResponseDTO;
import com.pneubras.api.dto.response.UserResponseDTO;
import com.pneubras.api.entity.Ticket;
import com.pneubras.api.entity.TicketPriority;
import com.pneubras.api.entity.TicketStatus;
import com.pneubras.api.entity.User;
import com.pneubras.api.entity.UserRole;

class TicketMapperTests {

    @Test
    void toResumeDTO_mapsSummaryFields() {
        LocalDateTime dueAt = LocalDateTime.of(2026, 5, 1, 10, 0);

        Ticket ticket = new Ticket();
        ticket.setId(42L);
        ticket.setTitle("Chamado X");
        ticket.setPriority(TicketPriority.MEDIUM);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setDueAt(dueAt);

        TicketResumeResponseDTO dto = TicketMapper.toResumeDTO(ticket);

        assertEquals(42L, dto.id());
        assertEquals("Chamado X", dto.title());
        assertEquals(TicketPriority.MEDIUM, dto.priority());
        assertEquals(TicketStatus.IN_PROGRESS, dto.status());
        assertEquals(dueAt, dto.dueAt());
    }

    @Test
    void toDetailsDTO_mapsAllFieldsAndNestedCreatedBy() {
        UUID creatorId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        LocalDateTime dueAt = LocalDateTime.of(2026, 6, 1, 18, 0);
        LocalDateTime createdAt = LocalDateTime.of(2026, 4, 1, 9, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 4, 2, 11, 0);

        User creator = new User();
        creator.setId(creatorId);
        creator.setName("Diana");
        creator.setEmail("diana@example.com");
        creator.setRole(UserRole.USER);
        creator.setPassword("encoded");

        Ticket ticket = new Ticket();
        ticket.setId(7L);
        ticket.setTitle("Detalhe");
        ticket.setDescription("Texto longo");
        ticket.setPriority(TicketPriority.CRITICAL);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setDueAt(dueAt);
        ticket.setCreatedBy(creator);
        ticket.setCreatedAt(createdAt);
        ticket.setUpdatedAt(updatedAt);

        TicketDetailsResponseDTO dto = TicketMapper.toDetailsDTO(ticket);

        assertEquals(7L, dto.id());
        assertEquals("Detalhe", dto.title());
        assertEquals("Texto longo", dto.description());
        assertEquals(TicketPriority.CRITICAL, dto.priority());
        assertEquals(TicketStatus.OPEN, dto.status());
        assertEquals(dueAt, dto.dueAt());
        assertEquals(createdAt, dto.createdAt());
        assertEquals(updatedAt, dto.updatedAt());

        UserResponseDTO createdBy = dto.createdBy();
        assertEquals(creatorId, createdBy.id());
        assertEquals("Diana", createdBy.name());
        assertEquals("diana@example.com", createdBy.email());
        assertEquals(UserRole.USER, createdBy.role());
    }
}
