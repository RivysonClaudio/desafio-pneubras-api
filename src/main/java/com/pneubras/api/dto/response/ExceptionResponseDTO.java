package com.pneubras.api.dto.response;

import java.time.LocalDateTime;

public record ExceptionResponseDTO(
    String message,
    LocalDateTime timestamp
) {}
