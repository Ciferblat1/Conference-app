package com.github.Ciferblat1.conference_app.security;

import java.util.Locale;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.github.Ciferblat1.conference_app.entity.User;
import com.github.Ciferblat1.conference_app.repository.UserRepository;

import lombok.RequiredArgsConstructor;


@Service 
@RequiredArgsConstructor 
public class AppUserDetailsService implements UserDetailsService{

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT))
            .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        return new AppUserDetails(user.getId(), user.getEmail(), user.getPasswordHash(), user.getRole());
    }
}
