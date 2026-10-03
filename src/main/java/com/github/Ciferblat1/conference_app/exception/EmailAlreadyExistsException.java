package com.github.Ciferblat1.conference_app.exception;

public class EmailAlreadyExistsException  extends RuntimeException{
    
    public EmailAlreadyExistsException(String email) {
        super("Пользователь с почтой " + email + " уже существует");
    }
}
