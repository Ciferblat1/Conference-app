package com.github.Ciferblat1.conference_app.dto;

import com.github.Ciferblat1.conference_app.entity.FundingType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateParticipantProfileRequest(

    @Size(max = 255, message = "firstName должен быть не длиннее 255 символов")
    @NotBlank 
    String firstName,

    @Size(max = 255, message = "surname должен быть не длиннее 255 символов")
    @NotBlank 
    String surname,

    @Size(max = 255, message = "patronymic должен быть не длиннее 255 символов")
    String patronymic,

    @Size(max = 50, message = "groupName должен быть не длиннее 50 символов")
    @NotBlank 
    String groupName,

    @Size(max = 255, message = "phone должен быть не длиннее 255 символов")
    @NotBlank 
    String phone,
    
    @NotNull 
    FundingType fundingType
) {
} 
