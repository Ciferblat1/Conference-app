package com.github.Ciferblat1.conference_app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import jakarta.servlet.http.HttpServletResponse;

@Configuration 
public class SecurityConfig {
    
    @Bean 
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form
                .loginProcessingUrl("/api/auth/login")
                .usernameParameter("email")
                .successHandler((request, response, authentication) ->
                    response.setStatus(HttpServletResponse.SC_OK))
                .failureHandler((request, response, exception) ->
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED))
            )
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler())
            );

        return http.build();
    }
}
