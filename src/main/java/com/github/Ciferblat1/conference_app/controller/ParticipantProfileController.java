package com.github.Ciferblat1.conference_app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.Ciferblat1.conference_app.dto.ParticipantProfileResponse;
import com.github.Ciferblat1.conference_app.dto.UpdateParticipantProfileRequest;
import com.github.Ciferblat1.conference_app.entity.ParticipantProfile;
import com.github.Ciferblat1.conference_app.security.AppUserDetails;
import com.github.Ciferblat1.conference_app.service.ParticipantProfileService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;



@RestController 
@RequiredArgsConstructor 
@RequestMapping("api/profile")
public class ParticipantProfileController {
    
    private final ParticipantProfileService participantProfileService;

    @PreAuthorize("hasRole('PARTICIPANT')")
    @GetMapping
    public ResponseEntity<ParticipantProfileResponse> getProfile(@AuthenticationPrincipal AppUserDetails principal) {

        ParticipantProfile profile = participantProfileService.getMyProfile(principal.getId());

        ParticipantProfileResponse response = toResponse(profile);

        return ResponseEntity.ok(response);
    }

    
    @PreAuthorize("hasRole('PARTICIPANT')")
    @PutMapping
    public ResponseEntity<ParticipantProfileResponse> updateProfile(
        @Valid @RequestBody UpdateParticipantProfileRequest request, 
        @AuthenticationPrincipal AppUserDetails principal
    ) {
        ParticipantProfile profile = participantProfileService.updateProfile(
            principal.getId(), 
            request.firstName(), 
            request.surname(), 
            request.patronymic(), 
            request.groupName(), 
            request.phone(), 
            request.fundingType()
        );

        ParticipantProfileResponse response = toResponse(profile);

        return ResponseEntity.ok(response);
    }



    private ParticipantProfileResponse toResponse(ParticipantProfile profile) {
        return new ParticipantProfileResponse(
            profile.getId(), 
            profile.getFirstName(), 
            profile.getSurname(), 
            profile.getPatronymic(), 
            profile.getGroupName(), 
            profile.getPhone(), 
            profile.getFundingType()
        );
    }
}
