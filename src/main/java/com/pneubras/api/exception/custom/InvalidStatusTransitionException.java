package com.pneubras.api.exception.custom;

import com.pneubras.api.exception.base.BadRequestException;

public class InvalidStatusTransitionException extends BadRequestException {

    public InvalidStatusTransitionException(String message) {
        super(message);
    }

    public InvalidStatusTransitionException() {
        super("Invalid status transition");
    }
}
