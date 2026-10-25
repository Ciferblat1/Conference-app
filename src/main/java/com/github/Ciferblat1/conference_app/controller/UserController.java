package com.github.Ciferblat1.conference_app.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.Ciferblat1.conference_app.dto.UserResponse;
import com.github.Ciferblat1.conference_app.entity.User;
import com.github.Ciferblat1.conference_app.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;


@Tag (
    name = "Пользователи",
    description = "Создание и получение пользователей"
)
@RestController 
@RequiredArgsConstructor 
@RequestMapping("api/users")
public class UserController {
    
    private final UserService userService;



    @Operation (
        summary = "Получение пользователя по ID"
    )
    @PreAuthorize("hasRole('ORGANIZER')")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("id") UUID id) {

        User user = userService.getUserById(id);

        UserResponse response = new UserResponse(
            user.getId(), user.getEmail(), user.getFullname(), user.getRole()
        );

        return ResponseEntity.ok(response);
    }


    @Operation (
        summary = "Получение всех пользователей"
    )
    @PreAuthorize("hasRole('ORGANIZER')")
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
