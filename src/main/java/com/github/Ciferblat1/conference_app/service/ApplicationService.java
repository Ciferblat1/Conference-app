package com.github.Ciferblat1.conference_app.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.Ciferblat1.conference_app.entity.Application;
import com.github.Ciferblat1.conference_app.entity.Direction;
import com.github.Ciferblat1.conference_app.entity.User;
import com.github.Ciferblat1.conference_app.exception.DirectionNotFoundException;
import com.github.Ciferblat1.conference_app.exception.UserNotFoundException;
import com.github.Ciferblat1.conference_app.repository.ApplicationRepository;
import com.github.Ciferblat1.conference_app.repository.DirectionRepository;
import com.github.Ciferblat1.conference_app.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final DirectionRepository directionRepository;

    @Transactional 
    public Application createApplication(UUID authorId, UUID directionID, String body) {

        if(authorId == null) {
            throw new IllegalArgumentException("ID автора не может быть null");
        }

        if(directionID == null) {
            throw new IllegalArgumentException("ID направления не может быть null");
        }

        if(body == null || body.isBlank()) {
            throw new IllegalArgumentException("Тело заявки не может быть пустым");
        }

        User author = userRepository.findById(authorId)
            .orElseThrow(() -> new UserNotFoundException(authorId));

        Direction direction = directionRepository.findById(directionID)
            .orElseThrow(() -> new DirectionNotFoundException(directionID));

        Application application = new Application(author, direction, body);

        applicationRepository.save(application);
        return application;
    }
}
