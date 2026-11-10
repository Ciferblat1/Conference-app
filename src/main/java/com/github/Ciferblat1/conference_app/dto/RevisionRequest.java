package com.github.Ciferblat1.conference_app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RevisionRequest(

    @NotBlank @Size(max = 255)
    String explanation
) {

}
