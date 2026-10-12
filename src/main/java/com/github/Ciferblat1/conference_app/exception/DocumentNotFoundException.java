package com.github.Ciferblat1.conference_app.exception;

import java.util.UUID;

public class DocumentNotFoundException extends RuntimeException{

    public DocumentNotFoundException(UUID id) {
        super("Документ с ID " + id + " не найден");
    }
}
