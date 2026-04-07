package com.pneubras.api.dto.request;

import jakarta.validation.constraints.Size;

public record TicketEditRequestDTO(
    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    String title,

    @Size(min = 3, max = 512, message = "Description must be between 3 and 512 characters")
    String description
) {}
