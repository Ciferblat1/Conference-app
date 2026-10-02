package com.github.Ciferblat1.conference_app.controller;

import java.util.UUID;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.Ciferblat1.conference_app.dto.CreateDirectionRequest;
import com.github.Ciferblat1.conference_app.dto.DirectionResponse;
import com.github.Ciferblat1.conference_app.entity.Direction;
import com.github.Ciferblat1.conference_app.service.DirectionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@RestController 
@RequestMapping("api/directions")
@RequiredArgsConstructor 
public class DirectionController {

    private final DirectionService directionService; 


    @PostMapping 
    public ResponseEntity<DirectionResponse> createDirection(
        @Valid @RequestBody  CreateDirectionRequest request
    ) {

        Direction direction = directionService.createDirection(request.title());

        DirectionResponse response = new DirectionResponse(direction.getId(), direction.getTitle());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DirectionResponse> getDirectionById(@PathVariable("id") UUID id) {

        Direction direction = directionService.getDirectionById(id);

        DirectionResponse response = new DirectionResponse(direction.getId(), direction.getTitle());

        return ResponseEntity.ok(response);
    }

    @GetMapping 
    public ResponseEntity<List<DirectionResponse>> getAllDirections() {

        List<Direction> directions = directionService.getAllDirections();

        List<DirectionResponse> response = directions.stream()
        .map(direction -> new DirectionResponse(direction.getId(), direction.getTitle())).toList();

        return ResponseEntity.ok(response);
    }
}
