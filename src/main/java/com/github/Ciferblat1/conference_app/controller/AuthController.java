package com.github.Ciferblat1.conference_app.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.Ciferblat1.conference_app.dto.RegisterRequest;
import com.github.Ciferblat1.conference_app.dto.UserResponse;
import com.github.Ciferblat1.conference_app.entity.User;
import com.github.Ciferblat1.conference_app.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController 
@RequiredArgsConstructor 
@RequestMapping("/api/auth")
public class AuthController {
    
    private final UserService userService;


    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest  request) {

        User user = userService.register(request.email(), request.fullname(), request.password());

        UserResponse response = new UserResponse(
            user.getId(), user.getEmail(), user.getFullname(), user.getRole()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
