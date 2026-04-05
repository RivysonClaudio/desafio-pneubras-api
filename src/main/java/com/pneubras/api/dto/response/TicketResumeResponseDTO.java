package com.pneubras.api.dto.response;

import java.time.LocalDateTime;

import com.pneubras.api.entity.TicketPriority;
import com.pneubras.api.entity.TicketStatus;

public record TicketResumeResponseDTO(
    Long id,
    String title,
    TicketPriority priority,
    TicketStatus status,
    LocalDateTime dueAt
) {}
