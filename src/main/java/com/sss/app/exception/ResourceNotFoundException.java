package com.sss.app.exception;

import java.util.UUID;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String entityName, UUID id) {
        super(entityName + " not found with id: " + id);
    }

    public ResourceNotFoundException(String entityName, String id) {
        super(entityName + " not found with id: " + id);
    }
}
