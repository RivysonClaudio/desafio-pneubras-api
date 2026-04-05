package com.pneubras.api.dto.response;

import java.util.UUID;

import com.pneubras.api.entity.UserRole;

public record RegisterResponseDTO(
    UUID id,
    String name,
    String email,
    UserRole role
) {}
