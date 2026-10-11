package com.github.Ciferblat1.conference_app.exception;

import java.util.UUID;

import com.github.Ciferblat1.conference_app.entity.ApplicationStatus;

public class InvalidApplicationStateException extends RuntimeException{

    public InvalidApplicationStateException(UUID id, ApplicationStatus status) {
        super("Заявка по ID " + id + " не допускает данного действия из-за своего статуса:" + status.toString());
    }
}
