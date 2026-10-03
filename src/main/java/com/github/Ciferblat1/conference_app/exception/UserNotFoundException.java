package com.github.Ciferblat1.conference_app.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(UUID id) {
        super("Пользователь по ID " + id + " не найден");
    }
}
