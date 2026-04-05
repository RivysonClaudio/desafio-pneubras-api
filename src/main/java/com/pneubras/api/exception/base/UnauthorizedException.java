package com.pneubras.api.exception.base;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException() {
        super("User not authenticated");
    }

}
