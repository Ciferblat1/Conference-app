package com.github.Ciferblat1.conference_app.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.github.Ciferblat1.conference_app.entity.ApplicationStatus;

public record ApplicationResponse(

    UUID id,
    UUID authorId,
    UUID directionId,
    ApplicationStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
} 