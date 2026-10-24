package com.github.Ciferblat1.conference_app.security;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.github.Ciferblat1.conference_app.entity.UserRole;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter 
@RequiredArgsConstructor 
public class AppUserDetails implements UserDetails{
    
    private final UUID id;
    private final String email;
    private final String passwordHash;
    private final UserRole role;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }


    @Override
    public @Nullable String getPassword() {
        return passwordHash;
    }


    @Override
    public String getUsername() {
        return email;
    }
}
