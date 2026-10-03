package com.github.Ciferblat1.conference_app.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;

import com.github.Ciferblat1.conference_app.entity.User;
import com.github.Ciferblat1.conference_app.entity.UserRole;
import com.github.Ciferblat1.conference_app.exception.EmailAlreadyExistsException;
import com.github.Ciferblat1.conference_app.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class UserService {
    
    private final UserRepository userRepository;

    @Transactional 
    public User createUser(String email, String fullname, UserRole role) {

        if(email == null || email.isBlank()) {
            throw new IllegalArgumentException("Почта пользователя не может быть пустой");
        }

        if(userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        if(fullname == null || fullname.isBlank()) {
            throw new IllegalArgumentException("Имя пользователя не может быть пустым");
        }

        User user = new User(email, fullname, role);
        userRepository.save(user);
        return user;
    }
}
