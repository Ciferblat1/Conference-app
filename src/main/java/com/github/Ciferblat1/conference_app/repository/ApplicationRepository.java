package com.github.Ciferblat1.conference_app.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.github.Ciferblat1.conference_app.entity.Application;

public interface ApplicationRepository extends JpaRepository<Application, UUID>{

}