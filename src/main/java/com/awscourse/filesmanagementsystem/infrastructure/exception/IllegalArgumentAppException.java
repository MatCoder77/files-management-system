package com.awscourse.filesmanagementsystem.infrastructure.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.PRECONDITION_FAILED)
public class IllegalArgumentAppException extends RuntimeException {

    public IllegalArgumentAppException(String message) {
        super(message);
    }

    public IllegalArgumentAppException(String message, Throwable cause) {
        super(message, cause);
    }

}
