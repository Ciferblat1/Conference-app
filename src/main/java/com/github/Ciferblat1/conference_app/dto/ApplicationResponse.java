package com.github.Ciferblat1.conference_app.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.github.Ciferblat1.conference_app.entity.ApplicationStatus;

public record ApplicationResponse(

    UUID id,
    UUID authorId,
    UUID directionId,
    AuthorSnapshotResponse snapshot,
    List<SupervisorResponse> supervisors,
    String title,
    String theses,
    List<String> referencesList,
    ApplicationStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
} 