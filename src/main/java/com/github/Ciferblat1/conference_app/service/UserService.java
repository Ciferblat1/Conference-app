package com.github.Ciferblat1.conference_app.service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.Ciferblat1.conference_app.dto.UserResponse;
import com.github.Ciferblat1.conference_app.entity.User;
import com.github.Ciferblat1.conference_app.entity.UserRole;
import com.github.Ciferblat1.conference_app.exception.EmailAlreadyExistsException;
import com.github.Ciferblat1.conference_app.exception.UserNotFoundException;
import com.github.Ciferblat1.conference_app.repository.DirectionRepository;
import com.github.Ciferblat1.conference_app.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DirectionRepository directionRepository;

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
            passwordEncoder.encode(password),
            null
        );


        userRepository.save(user);
        return user;
    }

    //Создать пользователя, связанного с направлением
    @Transactional 
    public UserResponse createDirectionUser(String email, String alias, UserRole role, String password, UUID directionId) {

        if(email == null || email.isBlank() || 
            alias == null || alias.isBlank() || 
            role == null || password == null || 
            password.isBlank() || directionId == null) {
                throw new IllegalArgumentException("Водные данные не могут быть пустыми");
            }

        if(userRepository.existsByEmail(email.trim().toLowerCase(Locale.ROOT))) {
            throw new EmailAlreadyExistsException(email);
        }

        if(role != UserRole.ORGANIZER && role != UserRole.EXPERT) {
            throw new IllegalArgumentException("Роль пользователя должна обеспичивать наличие связи с направлением");
        }

        if(!directionRepository.existsById(directionId)) {
            throw new IllegalArgumentException("Запрашиваемого направления не существует");
        }

        User user = new User(email, alias, role, passwordEncoder.encode(password), directionId);
        userRepository.save(user);

        return new UserResponse(user.getId(), user.getEmail(), user.getAlias(), user.getRole(), user.getDirectionId());
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
