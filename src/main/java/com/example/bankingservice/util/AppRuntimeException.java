package com.example.bankingservice.util;

public class AppRuntimeException extends RuntimeException {
    public AppRuntimeException() {
    }

    public AppRuntimeException(String message) {
        super(message);
    }
}
