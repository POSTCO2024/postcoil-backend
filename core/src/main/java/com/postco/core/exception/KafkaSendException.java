package com.postco.core.exception;

public class KafkaSendException extends RuntimeException {
    public KafkaSendException(String message) {
        super(message);
    }

    public KafkaSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
