package com.github.Ciferblat1.conference_app.dto;

import java.util.UUID;

import com.github.Ciferblat1.conference_app.entity.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateDirectionUserRequest(

        @NotBlank @Email @Size (max = 255) 
        String email,

        @NotBlank @Size(max = 255) 
        String alias,

        @NotBlank @Size(min = 8, max = 255) 
        String password,

        @NotNull 
        UserRole role,

        @NotNull 
        UUID directionId
) {
}