package com.github.Ciferblat1.conference_app.dto;

import com.github.Ciferblat1.conference_app.entity.FundingType;


public record AuthorSnapshotResponse(

    String firstName,
    String surname,
    String patronymic,
    String phone,
    String groupName,
    String email,
    FundingType fundingType

) {
} 