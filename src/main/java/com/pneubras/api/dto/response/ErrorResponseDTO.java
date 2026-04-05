package com.pneubras.api.dto.response;

import java.time.LocalDateTime;

public record ErrorResponseDTO(
    String message,
    LocalDateTime timestamp
) {}
