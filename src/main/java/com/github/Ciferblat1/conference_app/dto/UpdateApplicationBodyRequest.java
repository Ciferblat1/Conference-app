package com.github.Ciferblat1.conference_app.dto;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor 
@Setter 
@Getter 
public class UpdateApplicationBodyRequest {

    @NotNull(message = "Файл не может быть пустым")
    private MultipartFile file;
}
