package com.inpart.bane.exception;

public class ClientAlreadyMigratedException extends RuntimeException {
    public ClientAlreadyMigratedException(Long id) {
        super("Client with id %d has already been migrated".formatted(id));
    }
}

