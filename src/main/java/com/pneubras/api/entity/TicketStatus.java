package com.pneubras.api.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.pneubras.api.exception.base.BadRequestException;

public enum TicketStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CLOSED;

    @JsonCreator
    public static TicketStatus fromString(String value) {
        return switch (value) {
            case "OPEN",        "ABERTO"        -> OPEN;
            case "IN_PROGRESS", "EM_PROGRESSO"  -> IN_PROGRESS;
            case "RESOLVED",    "RESOLVIDO"     -> RESOLVED;
            case "CLOSED",      "FECHADO"       -> CLOSED;
            default -> throw new BadRequestException("Invalid status: " + value + " must be OPEN, IN_PROGRESS, RESOLVED or CLOSED");
        };
    }

    public Boolean canChangeTo(TicketStatus next) {
        return switch (this) {
            case OPEN -> next == IN_PROGRESS;
            case IN_PROGRESS -> next == RESOLVED;
            case RESOLVED -> next == CLOSED;
            case CLOSED -> false;
        };
    }
}
