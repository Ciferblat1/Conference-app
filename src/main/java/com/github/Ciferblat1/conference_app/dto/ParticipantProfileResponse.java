package com.github.Ciferblat1.conference_app.dto;

import java.util.UUID;

import com.github.Ciferblat1.conference_app.entity.FundingType;


public record ParticipantProfileResponse(

    UUID id,
    
    String firstName,

    String surname,

    String patronymic,

    String groupName,

    String phone,

    FundingType fundingType
) {
}
