package com.pneubras.api.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.pneubras.api.exception.base.BadRequestException;

public enum TicketPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    @JsonCreator
    public static TicketPriority fromString(String value) {
        return switch (value) {
            case "LOW",     "BAIXA"     -> LOW;
            case "MEDIUM",  "MÉDIA"     -> MEDIUM;
            case "HIGH",    "ALTA"      -> HIGH;
            case "CRITICAL","CRITICA"   -> CRITICAL;
            default -> throw new BadRequestException("Invalid priority: " + value + " must be LOW, MEDIUM, HIGH or CRITICAL");
        };
    }
}
