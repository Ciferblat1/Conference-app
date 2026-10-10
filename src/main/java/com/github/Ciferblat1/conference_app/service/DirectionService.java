package com.github.Ciferblat1.conference_app.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.github.Ciferblat1.conference_app.entity.Direction;
import com.github.Ciferblat1.conference_app.exception.DirectionNotFoundException;
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

    

    @Transactional(readOnly = true)
    public Direction getDirectionById(UUID id) {

        if(id == null) {
            throw new IllegalArgumentException("ID не может быть null");
        }

        return directionRepository.findById(id)
            .orElseThrow(() -> new DirectionNotFoundException(id));
    }



    @Transactional(readOnly = true)
    public List<Direction> getAllDirections() {

        return directionRepository.findAll();
    }
}
