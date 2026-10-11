package com.github.Ciferblat1.conference_app.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateApplicationBodyRequest(

    @NotBlank (message = "Текст заявки не может быть пустым")
    String body
) {

}
