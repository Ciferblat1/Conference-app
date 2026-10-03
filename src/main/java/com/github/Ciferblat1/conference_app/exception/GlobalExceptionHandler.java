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
}
