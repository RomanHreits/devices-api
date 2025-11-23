package com.example.devicesapi.exception;

public class BlockedResourceException extends RuntimeException {
    public BlockedResourceException(String message) {
        super(message);
    }
}
