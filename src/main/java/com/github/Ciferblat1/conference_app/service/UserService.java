package com.github.Ciferblat1.conference_app.service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.Ciferblat1.conference_app.entity.User;
import com.github.Ciferblat1.conference_app.entity.UserRole;
import com.github.Ciferblat1.conference_app.exception.EmailAlreadyExistsException;
import com.github.Ciferblat1.conference_app.exception.UserNotFoundException;
import com.github.Ciferblat1.conference_app.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional 
    public User register(String email, String fullname, String password) {

        if(email == null || email.isBlank()) {
            throw new IllegalArgumentException("Почта пользователя не может быть пустой");
        }

        String mormalisedEmail = email.trim().toLowerCase(Locale.ROOT);

        if(userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        if(fullname == null || fullname.isBlank()) {
            throw new IllegalArgumentException("Имя пользователя не может быть пустым");
        }

        User user = new User(
            mormalisedEmail, 
            fullname.trim(), 
            UserRole.PARTICIPANT, 
            passwordEncoder.encode(password)
        );

        userRepository.save(user);
        return user;
    }


    @Transactional(readOnly = true)
    public User getUserById(UUID id) {

        if(id == null) {
            throw new IllegalArgumentException("ID не может быть null");
        }

        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
    }

    
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
