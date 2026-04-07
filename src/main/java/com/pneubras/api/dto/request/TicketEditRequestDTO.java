package com.pneubras.api.dto.request;

import jakarta.validation.constraints.Size;

public record TicketEditRequestDTO(
    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    String title,

    String description
) {}
