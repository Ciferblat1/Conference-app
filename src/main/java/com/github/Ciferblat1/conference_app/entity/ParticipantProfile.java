package com.github.Ciferblat1.conference_app.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity 
@Table (name = "participant_profile")
@Getter 
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParticipantProfile {
    
    @Id 
    @Column (name = "user_id")
    private UUID id;


    @MapsId 
    @OneToOne (fetch = FetchType.LAZY, optional = false)
    @JoinColumn (name = "user_id", nullable = false)
    private User user;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "surname", nullable = false)
    private String surname;

    @Column(name = "patronymic", nullable = true)
    private String patronymic;

    @Column(name = "group_name", nullable = false, length = 50)
    private String groupName;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "funding_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private FundingType fundingType;


    public ParticipantProfile(
        User user,
        String firstName, 
        String surname, 
        String patronymic, 
        String groupName, 
        String phone, 
        FundingType fundingType
    ) {
        this.user = user;
        this.firstName = firstName;
        this.surname = surname;
        this.patronymic = patronymic;
        this.groupName = groupName;
        this.phone = phone;
        this.fundingType = fundingType;
    }
}
