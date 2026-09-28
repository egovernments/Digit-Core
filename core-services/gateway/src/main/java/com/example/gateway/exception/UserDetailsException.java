package com.example.gateway.exception;

import lombok.Getter;
import org.egov.tracer.model.CustomException;

@Getter
public class UserDetailsException extends CustomException {

    private final String description;

    public UserDetailsException(String code, String message, String description) {
        super(code, message);
        this.description = description;
    }
}
