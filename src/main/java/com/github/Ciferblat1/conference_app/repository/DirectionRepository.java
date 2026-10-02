package com.github.Ciferblat1.conference_app.repository;

import com.github.Ciferblat1.conference_app.entity.Direction;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DirectionRepository extends JpaRepository<Direction, UUID> {

}