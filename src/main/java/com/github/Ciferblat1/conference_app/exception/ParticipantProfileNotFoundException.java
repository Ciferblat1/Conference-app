package com.github.Ciferblat1.conference_app.exception;

import java.util.UUID;


public class ParticipantProfileNotFoundException extends RuntimeException{

    public ParticipantProfileNotFoundException(UUID id) {
        super("Профиль с ID " + id + " не найден");
    }
}
