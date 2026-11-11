package com.github.Ciferblat1.conference_app.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.Ciferblat1.conference_app.dto.ConferenceSettingsResponse;
import com.github.Ciferblat1.conference_app.entity.ConferenceSettings;
import com.github.Ciferblat1.conference_app.exception.ConferenceSettingsNotFoundException;
import com.github.Ciferblat1.conference_app.repository.ConferenceSettingsRepository;

import lombok.RequiredArgsConstructor;



@Service 
@RequiredArgsConstructor 
public class ConferenceSettingsService {

    private final ConferenceSettingsRepository conferenceSettingsRepository;

    /**
     * Изменить дедлайны конференции
     */
    @Transactional 
    public ConferenceSettingsResponse changeDeadlines(
        Instant submissionDeadline, Instant moderationDeadline
    ) {
        if(submissionDeadline == null || moderationDeadline == null) {
            throw new IllegalArgumentException("Дедлайны не могут быть пустыми");
        } 

        if(moderationDeadline.isBefore(submissionDeadline)) {
            throw new IllegalArgumentException("Дедлайн модерации не может быть раньше дедлайна подачи заявок");
        }

        short settingsId = 1;
        ConferenceSettings settings = conferenceSettingsRepository.findById(settingsId)
            .orElseThrow(() -> new ConferenceSettingsNotFoundException(settingsId));

        settings.changeDeadlines(submissionDeadline, moderationDeadline);

        conferenceSettingsRepository.save(settings);

        return new ConferenceSettingsResponse(submissionDeadline, moderationDeadline);
    }

    //Получить дедлайны конференции
    @Transactional 
    public ConferenceSettingsResponse getDeadlines() {

        short settingsId = 1;
        ConferenceSettings settings = conferenceSettingsRepository.findById(settingsId)
            .orElseThrow(() -> new ConferenceSettingsNotFoundException(settingsId));

        return new ConferenceSettingsResponse(settings.getSubmissionDeadline(), settings.getModerationDeadline());
    }
}
