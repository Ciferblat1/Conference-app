package com.github.Ciferblat1.conference_app.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity 
@Table(name = "users")
@Getter 
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "fullname", nullable = false)
    private String fullname;

    @Column(name = "role", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    public User(String email, String fullname, UserRole role, String passwordHash) {
        this.email = email;
        this.fullname = fullname;
        this.role = role;
        this.passwordHash = passwordHash;
    }
}
