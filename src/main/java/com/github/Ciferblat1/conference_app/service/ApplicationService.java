package com.github.Ciferblat1.conference_app.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.Ciferblat1.conference_app.entity.Application;
import com.github.Ciferblat1.conference_app.entity.ApplicationStatus;
import com.github.Ciferblat1.conference_app.entity.ConferenceSettings;
import com.github.Ciferblat1.conference_app.entity.Direction;
import com.github.Ciferblat1.conference_app.entity.User;
import com.github.Ciferblat1.conference_app.exception.ApplicationDeadlinePassedException;
import com.github.Ciferblat1.conference_app.exception.ApplicationNotFoundException;
import com.github.Ciferblat1.conference_app.exception.ConferenceSettingsNotFoundException;
import com.github.Ciferblat1.conference_app.exception.DirectionNotFoundException;
import com.github.Ciferblat1.conference_app.exception.InvalidApplicationStateException;
import com.github.Ciferblat1.conference_app.exception.UserNotFoundException;
import com.github.Ciferblat1.conference_app.repository.ApplicationRepository;
import com.github.Ciferblat1.conference_app.repository.ConferenceSettingsRepository;
import com.github.Ciferblat1.conference_app.repository.DirectionRepository;
import com.github.Ciferblat1.conference_app.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final DirectionRepository directionRepository;
    private final ConferenceSettingsRepository conferenceSettingsRepository;

    @Transactional 
    public Application createApplication(UUID authorId, UUID directionId, String body) {

        if(authorId == null) {
            throw new IllegalArgumentException("ID автора не может быть null");
        }

        if(directionId == null) {
            throw new IllegalArgumentException("ID направления не может быть null");
        }

        if(body == null || body.isBlank()) {
            throw new IllegalArgumentException("Тело заявки не может быть пустым");
        }

        checkSubmissionDeadline();

        User author = userRepository.findById(authorId)
            .orElseThrow(() -> new UserNotFoundException(authorId));

        Direction direction = directionRepository.findById(directionId)
            .orElseThrow(() -> new DirectionNotFoundException(directionId));

        Application application = new Application(author, direction, body);

        applicationRepository.save(application);
        return application;
    }



    @Transactional(readOnly = true)
    public List<Application> getAllApplications() {

        return  applicationRepository.findAll();

    }


    @Transactional(readOnly = true)
    public Application getApplicationById(UUID id) {

        if(id == null) {
            throw new IllegalArgumentException("ID не может быть null");
        }

        
        return applicationRepository.findById(id).orElseThrow(() -> new ApplicationNotFoundException(id));
    }


    @Transactional 
    public Application updateApplicationBody(UUID id, String newBody) {

        if(id == null || newBody == null) {
            throw new IllegalArgumentException("ID не может быть null");
        }

        if (newBody == null || newBody.isBlank()) {
            throw new IllegalArgumentException("Текст заявки не может быть пустым");
        }

        checkSubmissionDeadline();

        Application application = getApplicationById(id);

        if(application.getStatus() == ApplicationStatus.SUBMITTED) {
            application.updateBody(newBody);
        }
        else {
            throw new InvalidApplicationStateException(id, application.getStatus());
        }

        return application;
    }


    @Transactional 
    public Application setWithdrawStatus(UUID id) {

        if(id == null) {
            throw new IllegalArgumentException("ID не может быть null");
        }

        checkSubmissionDeadline();

        Application application = getApplicationById(id);

        if(application.getStatus() == ApplicationStatus.SUBMITTED) {
            application.withdraw();
        }
        else {
            throw new InvalidApplicationStateException(id, application.getStatus());
        }
        
        return application;
    }



    private void checkSubmissionDeadline() {

        Short settingsId = (short) 1;
        
        ConferenceSettings settings = conferenceSettingsRepository.findById(settingsId)
            .orElseThrow(() -> new ConferenceSettingsNotFoundException(settingsId));

        if(!Instant.now().isBefore(settings.getSubmissionDeadline())) {
            throw new ApplicationDeadlinePassedException();
        }
    }
}
