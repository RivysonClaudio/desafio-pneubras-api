package com.pneubras.api.dto.request;

import com.pneubras.api.entity.TicketStatus;

import jakarta.validation.constraints.NotNull;

public record TicketStatusUpdateRequestDTO(
    @NotNull(message = "Status is required")
    TicketStatus status
) {}
