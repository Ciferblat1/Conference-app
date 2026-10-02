package com.github.Ciferblat1.conference_app.exception;

import java.util.UUID;

public class DirectionNotFoundException extends RuntimeException{

    public DirectionNotFoundException(UUID id) {
        super("Направление с ID " + id + " не найдено");
    }
}
