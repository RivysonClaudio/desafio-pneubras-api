package com.pneubras.api.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.pneubras.api.exception.base.BadRequestException;

public enum TicketPriority {
    LOW("BAIXA"),
    MEDIUM("MEDIA"),
    HIGH("ALTA"),
    CRITICAL("CRITICA");

    private final String value;

    TicketPriority(String value) {
        this.value = value;
    }

    @JsonValue
    public String getPT_BRValue() {
        return value;
    }

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
