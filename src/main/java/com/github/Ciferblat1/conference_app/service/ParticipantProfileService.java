package com.github.Ciferblat1.conference_app.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.github.Ciferblat1.conference_app.entity.FundingType;
import com.github.Ciferblat1.conference_app.entity.ParticipantProfile;
import com.github.Ciferblat1.conference_app.entity.User;
import com.github.Ciferblat1.conference_app.exception.ParticipantProfileNotFoundException;
import com.github.Ciferblat1.conference_app.exception.UserNotFoundException;
import com.github.Ciferblat1.conference_app.repository.ParticipantProfileRepository;
import com.github.Ciferblat1.conference_app.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class ParticipantProfileService {
    
    private final ParticipantProfileRepository participantProfileRepository;
    private final UserRepository userRepository;


    @Transactional 
    public ParticipantProfile getMyProfile(UUID currentUserId) {
        if(currentUserId == null) {
            throw new IllegalArgumentException("id не может быть пустым");
        }


        ParticipantProfile profile = participantProfileRepository.findById(currentUserId)
            .orElseThrow(() -> new ParticipantProfileNotFoundException(currentUserId));

        return profile;
    }


    @Transactional 
    public ParticipantProfile updateProfile(
        UUID currentUserId,
        String firstName,
        String surname,
        String patronymic,
        String groupName,
        String phone,
        FundingType fundingType
    ) {
        if(currentUserId == null) {
            throw new IllegalArgumentException("ID пользователя не может быть null");
        }

        if (firstName == null || firstName.isBlank()
            || surname == null || surname.isBlank()
            || groupName == null || groupName.isBlank()
            || phone == null || phone.isBlank()
            || fundingType == null) {
        throw new IllegalArgumentException(
                "Имя, фамилия, группа, телефон и основа обучения обязательны"
        );
    }

        User currentUser = userRepository.findById(currentUserId)
            .orElseThrow(() -> new UserNotFoundException(currentUserId));

        Optional<ParticipantProfile> existingProfile = participantProfileRepository.findById(currentUserId);

        //Если профиль существует, обновить его
        if(existingProfile.isPresent()) {
            ParticipantProfile profile = existingProfile.get();

            profile.updateDetails(firstName, surname, patronymic, groupName, phone, fundingType);
            return profile;
        }

        //Иначе создать
        ParticipantProfile profile = new ParticipantProfile(currentUser,firstName, surname, patronymic, groupName, phone, fundingType);

        return participantProfileRepository.save(profile);
    }
}
