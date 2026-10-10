package com.github.Ciferblat1.conference_app.exception;

import java.util.UUID;

public class ApplicationNotFoundException extends RuntimeException{
    
    public ApplicationNotFoundException(UUID id) {
        super("Направление с ID " + id + " не найдено");
    }
}
