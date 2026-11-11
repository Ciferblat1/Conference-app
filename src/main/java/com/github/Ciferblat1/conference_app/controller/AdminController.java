package com.github.Ciferblat1.conference_app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.Ciferblat1.conference_app.dto.ChangeDeadlinesRequest;
import com.github.Ciferblat1.conference_app.dto.ConferenceSettingsResponse;
import com.github.Ciferblat1.conference_app.service.ConferenceSettingsService;
import com.github.Ciferblat1.conference_app.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController 
@RequiredArgsConstructor 
@RequestMapping ("api/admin")
public class AdminController {

    private final ConferenceSettingsService conferenceSettingsService;
    private final UserService userService;


    //Создание организатора направления


    //Создание члена экспертной комиссии

    //Изменение дедлайнов
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping ("/deadlines")
    public ResponseEntity<ConferenceSettingsResponse> changeDeadlines(
        @Valid @RequestBody ChangeDeadlinesRequest request
    ) {

        ConferenceSettingsResponse response = 
            conferenceSettingsService.changeDeadlines(request.submissionDeadline(), request.moderationDeadline());

        return ResponseEntity.ok(response);
    }

    
    //Получение дедлайнов
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/deadlines")
    public ResponseEntity<ConferenceSettingsResponse> getDeadlines() {

        ConferenceSettingsResponse response = conferenceSettingsService.getDeadlines();

        return ResponseEntity.ok(response);
    }
}
