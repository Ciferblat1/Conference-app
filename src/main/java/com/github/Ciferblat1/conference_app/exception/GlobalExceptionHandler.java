package com.github.Ciferblat1.conference_app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice 
public class GlobalExceptionHandler {
    
    @ExceptionHandler(DirectionNotFoundException.class)
    public ProblemDetail handleDirectionNotFound(DirectionNotFoundException exception) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND, exception.getMessage()
        );

        problem.setTitle("Направление не найдено");

        return problem;
    }


    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ProblemDetail handleEmailAlreadyExists(EmailAlreadyExistsException exception) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT, exception.getMessage()
        );

        problem.setTitle("email уже существует");

        return problem;
    }


    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFound(UserNotFoundException exception) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND, exception.getMessage()
        );

        problem.setTitle("Пользователь не найден");

        return problem;
    }



    @ExceptionHandler(ApplicationNotFoundException.class)
    public ProblemDetail handleApplicationNotFound(ApplicationNotFoundException exception) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND, exception.getMessage()
        );

        problem.setTitle("Заявка не найдена");

        return problem;
    }


    @ExceptionHandler(InvalidApplicationStateException.class)
    public ProblemDetail handleInvalidApplicationState(InvalidApplicationStateException exception) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());

        problem.setTitle("Действие невозможно из-за статуса заявки");

        return problem;
    }


    @ExceptionHandler(ConferenceSettingsNotFoundException.class)
    public ProblemDetail handleConferenceSettingsNotFound(ConferenceSettingsNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());

        problem.setTitle("Настройки конференции не найдены");

        return problem;
    }



    @ExceptionHandler(ApplicationDeadlinePassedException.class)
    public ProblemDetail handleApplicationDeadlinePassed(ApplicationDeadlinePassedException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());

        problem.setTitle("Дедлайн подачи заявления прошел");

        return problem;
    }


    @ExceptionHandler(DocumentNotFoundException.class)
    public ProblemDetail handleDocumentNotFound(DocumentNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());

        problem.setTitle("Документ не найден");

        return problem;
    }

    @ExceptionHandler(ApplicationAccessDeniedException.class)
    public ProblemDetail handleApplicationAccessDeniedException(ApplicationAccessDeniedException exception) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());

        problem.setTitle("Нет доступа к заявке");

        return problem;
    }
}
