package com.github.Ciferblat1.conference_app.exception;

public class ApplicationDeadlinePassedException extends RuntimeException{

    public ApplicationDeadlinePassedException() {
        super("Дедлайн подачи зявки уже прошел");
    }
}
