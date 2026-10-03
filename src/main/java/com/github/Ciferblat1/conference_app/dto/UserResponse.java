package com.github.Ciferblat1.conference_app.dto;

import java.util.UUID;

import com.github.Ciferblat1.conference_app.entity.UserRole;

public record UserResponse(

    UUID id,
    String email,
    String fullname,
    UserRole role
) {
} 