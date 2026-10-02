package com.github.Ciferblat1.conference_app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDirectionRequest(
    
    @NotBlank 
    @Size(max = 255, message = "Название направления должно содержать не более 255 символов")
    String title

) {

} 
