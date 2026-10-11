package com.github.Ciferblat1.conference_app.exception;

public class ConferenceSettingsNotFoundException extends RuntimeException{
    public ConferenceSettingsNotFoundException(Short id) {
        super("Настройки конференции не по ID " + id + " не найдены");
    }
}
