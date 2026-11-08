package com.github.Ciferblat1.conference_app.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;



@Embeddable 
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ApplicationAuthorSnapshot {
    
    @Column(name = "author_first_name", nullable = false)
    private String firstName;

    @Column(name = "author_surname", nullable = false)
    private String surname;

    @Column(name = "author_patronymic", nullable = true)
    private String patronymic;

    @Column(name = "author_phone", nullable = false)
    private String phone;

    @Column(name = "author_group_name", nullable = false, length = 50)
    private String groupName;

    @Column(name = "author_email", nullable = false)
    private String email;

    @Column(name = "author_funding_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private FundingType fundingType;


    public ApplicationAuthorSnapshot(
        String firstName,
        String surname,
        String patronymic,
        String phone,
        String groupName,
        String email,
        FundingType fundingType
    ){
        this.firstName = firstName;
        this.surname = surname;
        this.patronymic = patronymic;
        this.phone = phone;
        this.groupName = groupName;
        this.email = email;
        this.fundingType = fundingType;
    }
}
