package com.github.Ciferblat1.conference_app.dto;

import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter 
@Setter 
@NoArgsConstructor 
public class CreateApplicationRequest {


    @NotNull(message = "Направление обязательно")
    private UUID directionId;

    @NotNull(message = "Файл обязателен")
    private MultipartFile file;
} 