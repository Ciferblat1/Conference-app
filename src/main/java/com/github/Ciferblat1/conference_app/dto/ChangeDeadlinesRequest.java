package com.github.Ciferblat1.conference_app.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;

public record ChangeDeadlinesRequest(
    
    @NotNull 
    Instant submissionDeadline,

    @NotNull 
    Instant moderationDeadline
) {

}
