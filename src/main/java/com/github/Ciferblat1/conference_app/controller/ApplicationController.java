package com.github.Ciferblat1.conference_app.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.Ciferblat1.conference_app.dto.ApplicationResponse;
import com.github.Ciferblat1.conference_app.dto.CreateApplicationRequest;
import com.github.Ciferblat1.conference_app.dto.UpdateApplicationBodyRequest;
import com.github.Ciferblat1.conference_app.entity.Application;
import com.github.Ciferblat1.conference_app.service.ApplicationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController 
@RequiredArgsConstructor 
@RequestMapping("api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    
    @PostMapping 
    public ResponseEntity<ApplicationResponse> createApplication(
        @Valid @RequestBody CreateApplicationRequest request
    ) {

        Application application = applicationService.createApplication(
            request.authorId(), request.directionId(), request.body()
        );


        ApplicationResponse response = toResponse(application);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PostMapping("{id}/withdraw")
    public ResponseEntity<ApplicationResponse> setWithdrowStatus(@PathVariable UUID id) {

        Application application = applicationService.setWithdrawStatus(id);

        ApplicationResponse response = toResponse(application);

        return ResponseEntity.ok(response);
    }


    @GetMapping  
    public ResponseEntity<List<ApplicationResponse>> getAllApplications() {

        List<Application> applications = applicationService.getAllApplications();

        List<ApplicationResponse> response = applications.stream()
            .map(application -> toResponse(application)).toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplicationById(@PathVariable("id") UUID id) {

        Application application = applicationService.getApplicationById(id);

        ApplicationResponse response = toResponse(application);
        
        return ResponseEntity.ok(response);
    }


    @PatchMapping("/{id}")
    public ResponseEntity<ApplicationResponse> updateApplicationBody(
        @PathVariable("id") UUID id, 
        @Valid @RequestBody  UpdateApplicationBodyRequest request
    ) {

        Application application = applicationService.updateApplicationBody(id, request.body());

        ApplicationResponse response = toResponse(application);
        
        return ResponseEntity.ok(response);
    }


    private ApplicationResponse toResponse(Application application) {
        return new ApplicationResponse(
            application.getId(), 
            application.getAuthor().getId(), 
            application.getDirection().getId(), 
            application.getStatus(), 
            application.getBody(), 
            application.getCreatedAt(), 
            application.getUpdatedAt()
        );
    }
}
