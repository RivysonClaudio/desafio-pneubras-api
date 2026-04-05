package com.pneubras.api.exception.custom;

import com.pneubras.api.exception.base.BadRequestException;

public class EmailAlreadyExistsException extends BadRequestException {

    public EmailAlreadyExistsException(String message) {
        super(message);
    }

    public EmailAlreadyExistsException() {
        super("Email already exists");
    }

}
