package com.github.Ciferblat1.conference_app.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.Ciferblat1.conference_app.dto.ApplicationResponse;
import com.github.Ciferblat1.conference_app.dto.AuthorSnapshotResponse;
import com.github.Ciferblat1.conference_app.dto.CreateApplicationRequest;
import com.github.Ciferblat1.conference_app.dto.RevisionRequest;
import com.github.Ciferblat1.conference_app.dto.SupervisorRequest;
import com.github.Ciferblat1.conference_app.dto.SupervisorResponse;
import com.github.Ciferblat1.conference_app.dto.UpdateApplicationRequest;
import com.github.Ciferblat1.conference_app.entity.Application;
import com.github.Ciferblat1.conference_app.entity.ApplicationAuthorSnapshot;
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

        Application application = applicationService.createApplication(
            principal.getId(), 
            request.getDirectionId(), 
            supervisors,
            request.getTitle(), 
            request.getTheses(), 
            request.getReferencesList()
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

        List<Application> applications = applicationService.getApplicationsByAuthorId(principal.getId());

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
    public ResponseEntity<List<ApplicationResponse>> getAllApplications(
        @AuthenticationPrincipal AppUserDetails principal
    ) {

        List<Application> applications = applicationService.getAllApplications(principal.getId());

        List<ApplicationResponse> response = applications.stream()
            .map(application -> toResponse(application)).toList();

        return ResponseEntity.ok(response);
    }



    @Operation(
        summary = "Получить заявку по ID"
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplicationById(
        @PathVariable("id") UUID id, 
        @AuthenticationPrincipal AppUserDetails principal) {

        Application application = applicationService.getApplicationById(id, principal.getId());

        ApplicationResponse response = toResponse(application);
        
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

        Application application = applicationService.updateApplication(
            principal.getId(), 
            id, 
            supervisors, 
            request.getTitle(), 
            request.getTheses(), 
            request.getReferencesList(),
            request.isRefreshAuthorSnapshot()
        );

        ApplicationResponse response = toResponse(application);
        
        return ResponseEntity.ok(response);
    }



    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping("{id}/approve")
    public ResponseEntity<ApplicationResponse> setApprovedStatus(
        @AuthenticationPrincipal AppUserDetails principal,
        @PathVariable UUID id
    ) {

        Application application = applicationService.setApprovedStatus(id, principal.getId());

        ApplicationResponse response = toResponse(application);

        return ResponseEntity.ok(response);
    }


    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping("{id}/reject")
    public ResponseEntity<ApplicationResponse> setRejectedStatus(
        @AuthenticationPrincipal AppUserDetails principal,
        @PathVariable UUID id
    ) {

        Application application = applicationService.setRejectedStatus(id, principal.getId());

        ApplicationResponse response = toResponse(application);

        return ResponseEntity.ok(response);
    }


    @PreAuthorize("hasRole('ORGANIZER')")
    @PostMapping("{id}/revision")
    public ResponseEntity<ApplicationResponse> setRevisionStatus(
        @AuthenticationPrincipal AppUserDetails principal,
        @Valid @RequestBody RevisionRequest request,
        @PathVariable UUID id
    ) {

        Application application = applicationService.setRevisionStatus(id, principal.getId(), request.explanation());

        ApplicationResponse response = toResponse(application);

        return ResponseEntity.ok(response);
    }


    //Преобразовать сущность заявки в ответ заявки
    private ApplicationResponse toResponse(Application application) {
    return new ApplicationResponse(
            application.getId(),
            application.getAuthor().getId(),
            application.getDirection().getId(),
            toAuthorSnapshotResponse(application.getAuthorSnapshot()),
            application.getSupervisors().stream()
                    .map(this::toSupervisorResponse)
                    .toList(),
            application.getTitle(),
            application.getTheses(),
            List.copyOf(application.getReferencesList()),
            application.getStatus(),
            application.getCreatedAt(),
            application.getUpdatedAt(),
            application.getExplanation()
    );
}

    //Преобразовать снапшот данных автора в ответ снапшота
    private AuthorSnapshotResponse toAuthorSnapshotResponse(
            ApplicationAuthorSnapshot snapshot
    ) {
        return new AuthorSnapshotResponse(
                snapshot.getFirstName(),
                snapshot.getSurname(),
                snapshot.getPatronymic(),
                snapshot.getPhone(),
                snapshot.getGroupName(),
                snapshot.getEmail(),
                snapshot.getFundingType()
        );
    }


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

    //Преобразовать сущность научного руководителя в ответ руководителя
    private SupervisorResponse toSupervisorResponse(
            ApplicationSupervisor supervisor
    ) {
        return new SupervisorResponse(
                supervisor.getFirstName(),
                supervisor.getSurname(),
                supervisor.getPatronymic(),
                supervisor.getPosition(),
                supervisor.getDepartment()
        );
    }
}
