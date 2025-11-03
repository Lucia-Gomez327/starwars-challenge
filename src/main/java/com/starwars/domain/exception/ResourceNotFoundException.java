package com.starwars.domain.exception;

public class ResourceNotFoundException extends DomainException {
    public ResourceNotFoundException(String resource, String identifier) {
        super(String.format("%s not found with identifier: %s", resource, identifier));
    }
}


