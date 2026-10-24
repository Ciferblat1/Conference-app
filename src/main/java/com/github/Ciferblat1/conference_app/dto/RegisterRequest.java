package com.github.Ciferblat1.conference_app.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

    @Size(max = 255, message = "email должен быть не длиннее 255 символов")
    @NotBlank 
    String email,

    @Size(max = 255, message = "имя должно быть не длиннее 255 символов")
    @NotBlank 
    String fullname,

    @NotBlank 
    @Size(min = 8, max = 72, message = "Пароль должен быть от 8 до 72 символов")
    String password
) {
} 