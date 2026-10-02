package com.github.Ciferblat1.conference_app.service;

import org.springframework.stereotype.Service;

import com.github.Ciferblat1.conference_app.entity.Direction;
import com.github.Ciferblat1.conference_app.repository.DirectionRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class DirectionService {
    
    private final DirectionRepository directionRepository;

    @Transactional 
    public Direction createDirection(String title) {

        if(title == null || title.isBlank()) {
            throw new IllegalArgumentException("Название направления не может быть пустым");
        }

        Direction direction = new Direction(title);

        return directionRepository.save(direction);
    } 
}
