package com.github.Ciferblat1.conference_app.dto;

import com.github.Ciferblat1.conference_app.entity.UserRole;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(

    @Size(max = 255, message = "email должен быть не длиннее 255 символов")
    @NotBlank 
    String email,

    @Size(max = 255, message = "email должен быть не длиннее 255 символов")
    @NotBlank 
    String fullname,

    @Size(max = 50, message = "Роль должна быть не длиннее 50 символов")
    UserRole role
) {
} 