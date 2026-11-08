package com.github.Ciferblat1.conference_app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SupervisorRequest(

    @NotBlank @Size(max = 255)
    String firstName,

    @NotBlank @Size(max = 255)
    String surname,

    @Size(max = 255)
    String patronymic,

    @NotBlank @Size(max = 255)
    String position,
    
    @NotBlank @Size(max = 255)
    String department
) {
} 
