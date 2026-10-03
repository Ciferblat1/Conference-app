package com.github.Ciferblat1.conference_app.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.Ciferblat1.conference_app.dto.CreateUserRequest;
import com.github.Ciferblat1.conference_app.dto.UserResponse;
import com.github.Ciferblat1.conference_app.entity.User;
import com.github.Ciferblat1.conference_app.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController 
@RequiredArgsConstructor 
@RequestMapping("api/users")
public class UserController {
    
    private final UserService userService;

    @PostMapping 
    public ResponseEntity<UserResponse> createUser(
        @Valid  @RequestBody CreateUserRequest request
    ) {

        User user = userService.createUser(request.email(), request.fullname(), request.role());

        UserResponse response = new UserResponse(
            user.getId(), user.getEmail(), user.getFullname(), user.getRole()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("id") UUID id) {

        User user = userService.getUserById(id);

        UserResponse response = new UserResponse(
            user.getId(), user.getEmail(), user.getFullname(), user.getRole()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping 
    public ResponseEntity<List<UserResponse>> getAllUsers() {

        List<User> users = userService.getAllUsers();

        List<UserResponse> response = users.stream()
            .map(user -> new UserResponse(
                user.getId(), 
                user.getEmail(), 
                user.getFullname(), 
                user.getRole())).toList();

        return ResponseEntity.ok(response);
    }
}
