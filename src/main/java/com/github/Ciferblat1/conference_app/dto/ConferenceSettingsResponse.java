package com.github.Ciferblat1.conference_app.dto;

import java.time.Instant;

public record ConferenceSettingsResponse(

    Instant submissionDeadline,

    Instant moderationDeadline
) {

}
