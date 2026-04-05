package com.pneubras.api.dto.response;

import java.time.LocalDateTime;

import com.pneubras.api.entity.TicketPriority;
import com.pneubras.api.entity.TicketStatus;

public record TicketDetailsResponseDTO(
    Long id,
    String title,
    String description,
    TicketPriority priority,
    TicketStatus status,
    LocalDateTime dueAt,
    UserResponseDTO createdBy,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
