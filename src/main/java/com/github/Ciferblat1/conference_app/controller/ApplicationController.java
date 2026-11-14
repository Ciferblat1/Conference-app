package com.github.Ciferblat1.conference_app.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.Ciferblat1.conference_app.dto.ApplicationExpertResponse;
import com.github.Ciferblat1.conference_app.dto.ApplicationResponse;
import com.github.Ciferblat1.conference_app.dto.ApplicationTransferRequest;
import com.github.Ciferblat1.conference_app.dto.CreateApplicationRequest;
import com.github.Ciferblat1.conference_app.dto.RevisionRequest;
import com.github.Ciferblat1.conference_app.dto.SupervisorRequest;
import com.github.Ciferblat1.conference_app.dto.UpdateApplicationRequest;
import com.github.Ciferblat1.conference_app.entity.ApplicationSupervisor;
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


    @PreAuthorize("hasRole('PARTICIPANT')")
    @PostMapping 
    public ResponseEntity<ApplicationResponse> createApplication(
        @AuthenticationPrincipal AppUserDetails principal,
        @Valid @RequestBody  CreateApplicationRequest request
    ) {

        List<ApplicationSupervisor> supervisors = toSupervisorsEntity(request.getSupervisors());

        ApplicationResponse response = applicationService.createApplication(
            principal.getId(), 
            request.getDirectionId(), 
            supervisors,
            request.getTitle(), 
            request.getTheses(), 
            request.getReferencesList()
        );

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

        ApplicationResponse response = applicationService.setWithdrawStatus(id, principal.getId());

        return ResponseEntity.ok(response);
    }


    @Operation(
        summary = "Получить заявки авторства текущего пользователя"
    )
    @PreAuthorize("hasRole('PARTICIPANT')")
    @GetMapping("/me")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications(
        @AuthenticationPrincipal AppUserDetails principal
    ) {

        List<ApplicationResponse> response = applicationService.getApplicationsByAuthorId(principal.getId());

        return ResponseEntity.ok(response);
    }


    @Operation(
        summary = "Получить все заявки, доступные организатору направления"
    )
    @PreAuthorize("hasRole('ORGANIZER')")
    @GetMapping  
    public ResponseEntity<List<ApplicationResponse>> getAllApplications(
        @AuthenticationPrincipal AppUserDetails principal
    ) {

        List<ApplicationResponse> response = applicationService.getAllApplications(principal.getId());

        return ResponseEntity.ok(response);
    }



    @Operation(
        summary = "Получить заявку по ID"
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplicationById(
        @PathVariable("id") UUID id, 
        @AuthenticationPrincipal AppUserDetails principal) {

        ApplicationResponse response = applicationService.getApplicationById(id, principal.getId());

        return ResponseEntity.ok(response);
    }



    @PreAuthorize("hasRole('PARTICIPANT')")
    @PutMapping ("/{id}")
    public ResponseEntity<ApplicationResponse> updateApplicationBody(
        @PathVariable("id") UUID id, 
        @Valid @RequestBody UpdateApplicationRequest request,
        @AuthenticationPrincipal AppUserDetails principal
    ) {

        List<ApplicationSupervisor> supervisors = toSupervisorsEntity(request.getSupervisors());

        ApplicationResponse response = applicationService.updateApplication(
            principal.getId(), 
            id, 
            supervisors, 
            request.getTitle(), 
            request.getTheses(), 
            request.getReferencesList(),
            request.isRefreshAuthorSnapshot()
        );

        return ResponseEntity.ok(response);
    }



    //===============================МОДЕРАЦИЯ ЗАЯВОК ОРГАНИЗАТОРОМ НАПРАВЛЕНИЯ===============================

    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping("{id}/approve")
    public ResponseEntity<ApplicationResponse> setApprovedStatus(
        @AuthenticationPrincipal AppUserDetails principal,
        @PathVariable UUID id
    ) {

        ApplicationResponse response = applicationService.setApprovedStatus(id, principal.getId());

        return ResponseEntity.ok(response);
    }


    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping("{id}/reject")
    public ResponseEntity<ApplicationResponse> setRejectedStatus(
        @AuthenticationPrincipal AppUserDetails principal,
        @PathVariable UUID id
    ) {

        ApplicationResponse response = applicationService.setRejectedStatus(id, principal.getId());

        return ResponseEntity.ok(response);
    }


    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping("{id}/revision")
    public ResponseEntity<ApplicationResponse> setRevisionStatus(
        @AuthenticationPrincipal AppUserDetails principal,
        @Valid @RequestBody RevisionRequest request,
        @PathVariable UUID id
    ) {

        ApplicationResponse response = applicationService.setRevisionStatus(id, principal.getId(), request.explanation());

        return ResponseEntity.ok(response);
    }




    //===============================ВАИМОДЕЙСТВИЕ ЭКСПЕРТА С ЗАЯВКАМИ===============================

    @PreAuthorize("hasRole('EXPERT')")
    @GetMapping("/expert")
    public ResponseEntity<List<ApplicationExpertResponse>> getAllApplicationsForExpert(
        @AuthenticationPrincipal AppUserDetails principal
    ) {
        List<ApplicationExpertResponse> response = applicationService.getApplicationsForExpert(principal.getId());

        return ResponseEntity.ok(response);
    }

    //===============================ПЕРЕВОД ЗАЯВОК==========================================

    //Запросить перевод заявки на другое направление
    @PreAuthorize("hasRole('ORGANIZER')")
    @PatchMapping("/{applicationId}/transfer")
    public ResponseEntity<ApplicationResponse> requestTransferApplication(
        @AuthenticationPrincipal AppUserDetails principal,
        @Valid @RequestBody ApplicationTransferRequest request,
        @PathVariable UUID applicationId
    ) {
        ApplicationResponse response = applicationService
            .requestTransferApplication(principal.getId(), applicationId, request.targetDirectionId());

        return ResponseEntity.ok(response);
    }

    //Одобрить перевод заявки
    @PreAuthorize("hasRole('ORGANIZER')")
    @PatchMapping("/{applicationId}/transfer/apply")
    public ResponseEntity<ApplicationResponse> applyTransferApplication(
        @AuthenticationPrincipal AppUserDetails principal,
        @PathVariable UUID applicationId
    ) {
        ApplicationResponse response = applicationService
            .applyTransferApplication(principal.getId(), applicationId);

        return ResponseEntity.ok(response);
    }

    //Отклонить перевод заявки
    @PreAuthorize("hasRole('ORGANIZER')")
    @PatchMapping("/{applicationId}/transfer/reject")
    public ResponseEntity<ApplicationResponse> rejectTransferApplication(
        @AuthenticationPrincipal AppUserDetails principal,
        @PathVariable UUID applicationId
    ) {
        ApplicationResponse response = applicationService
            .rejectTransferApplication(principal.getId(), applicationId);

        return ResponseEntity.ok(response);
    }

    //Просмотреть все заявки, ожидающие перевода
    @PreAuthorize("hasRole('ORGANIZER')")
    @GetMapping("/transfers")
    public ResponseEntity<List<ApplicationResponse>> getPendingTransferApplications(
        @AuthenticationPrincipal AppUserDetails principal
    ) {
        List<ApplicationResponse> response = applicationService.getPendingTransferApplications(principal.getId());

        return ResponseEntity.ok(response);
    }

    //===============================ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ===============================


    //Преобразует список заявок на создание научных руководителей в список сущностей 
    private List<ApplicationSupervisor> toSupervisorsEntity(List<SupervisorRequest> supervisors) {

        return  supervisors.stream().map(
            supervisor -> new ApplicationSupervisor(
                supervisor.firstName(), 
                supervisor.surname(), 
                supervisor.patronymic(), 
                supervisor.department(), 
                supervisor.position()
            )).toList();
    } 

}
