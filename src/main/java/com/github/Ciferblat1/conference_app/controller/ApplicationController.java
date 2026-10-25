package com.github.Ciferblat1.conference_app.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.Ciferblat1.conference_app.dto.ApplicationResponse;
import com.github.Ciferblat1.conference_app.dto.CreateApplicationRequest;
import com.github.Ciferblat1.conference_app.dto.UpdateApplicationBodyRequest;
import com.github.Ciferblat1.conference_app.entity.Application;
import com.github.Ciferblat1.conference_app.security.AppUserDetails;
import com.github.Ciferblat1.conference_app.service.ApplicationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@Tag(
    name = "Заявки",
    description = "Подача, получение, замена документа и отзыв заявок"
)
@RestController 
@RequiredArgsConstructor 
@RequestMapping("api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    @Operation(
        summary = "Создать заявку"
    )
    @PreAuthorize("hasRole('PARTICIPANT')")
    @PostMapping 
    public ResponseEntity<ApplicationResponse> createApplication(
        @AuthenticationPrincipal AppUserDetails principal,
        @Valid @ModelAttribute CreateApplicationRequest request
    ) {

        Application application = applicationService.createApplication(
            principal.getId(), request.getDirectionId(), request.getFile()
        );

        ApplicationResponse response = toResponse(application);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @Operation(
        summary = "Отозвать заявку"
    )
    @PreAuthorize("hasRole('PARTICIPANT')")
    @PostMapping("{id}/withdraw")
    public ResponseEntity<ApplicationResponse> setWithdrowStatus(
        @AuthenticationPrincipal AppUserDetails principal,
        @PathVariable UUID id
    ) {

        Application application = applicationService.setWithdrawStatus(id, principal.getId());

        ApplicationResponse response = toResponse(application);

        return ResponseEntity.ok(response);
    }


    @Operation(
        summary = "Получить заявки текущего пользователя"
    )
    @PreAuthorize("hasRole('PARTICIPANT')")
    @GetMapping("/me")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications(
        @AuthenticationPrincipal AppUserDetails principal
    ) {

        List<Application> applications = applicationService.getApplicationByAuthorId(principal.getId());

        List<ApplicationResponse> response = applications.stream()
            .map(application -> toResponse(application))
            .toList();

        return ResponseEntity.ok(response);
    }


    @Operation(
        summary = "Получить все заявки"
    )
    @PreAuthorize("hasRole('ORGANIZER')")
    @GetMapping  
    public ResponseEntity<List<ApplicationResponse>> getAllApplications() {

        List<Application> applications = applicationService.getAllApplications();

        List<ApplicationResponse> response = applications.stream()
            .map(application -> toResponse(application)).toList();

        return ResponseEntity.ok(response);
    }



    @Operation(
        summary = "Получить заявку по ID"
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplicationById(@PathVariable("id") UUID id) {

        Application application = applicationService.getApplicationById(id);

        ApplicationResponse response = toResponse(application);
        
        return ResponseEntity.ok(response);
    }


    @Operation(
        summary = "Заменить документ заявки",
        description = """
                Принимает новый файл DOC или DOCX размером до 10 МиБ.
                Замена разрешена до T1 для поданных и отозванных заявок.
                Предыдущая версия не сохраняется, статус не изменяется.
                """
    )
    @PreAuthorize("hasRole('PARTICIPANT')")
    @PatchMapping("/{id}")
    public ResponseEntity<ApplicationResponse> updateApplicationBody(
        @PathVariable("id") UUID id, 
        @Valid @ModelAttribute  UpdateApplicationBodyRequest request,
        @AuthenticationPrincipal AppUserDetails principal
    ) {

        Application application = applicationService.updateApplicationDocument(id, request.getFile(), principal.getId());

        ApplicationResponse response = toResponse(application);
        
        return ResponseEntity.ok(response);
    }


    
    private ApplicationResponse toResponse(Application application) {
        return new ApplicationResponse(
            application.getId(), 
            application.getAuthor().getId(), 
            application.getDirection().getId(), 
            application.getStatus(), 
            application.getCreatedAt(), 
            application.getUpdatedAt()
        );
    }
}
