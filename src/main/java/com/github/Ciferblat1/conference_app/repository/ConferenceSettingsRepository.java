package com.github.Ciferblat1.conference_app.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.Ciferblat1.conference_app.entity.ConferenceSettings;

public interface ConferenceSettingsRepository extends JpaRepository<ConferenceSettings, Short>{
    
}
