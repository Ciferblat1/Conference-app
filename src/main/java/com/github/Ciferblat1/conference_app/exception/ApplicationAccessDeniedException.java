package com.github.Ciferblat1.conference_app.exception;

import java.util.UUID;

public class ApplicationAccessDeniedException extends RuntimeException{
    public ApplicationAccessDeniedException(UUID applicationId) {
        super("Нет доступа к заявке с id:" + applicationId);
    }
}
