package com.github.Ciferblat1.conference_app.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity 
@Getter 
@Table (name = "conference_settings")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConferenceSettings {

    @jakarta.persistence.Id 
    private short id;

    @Column(name = "submission_deadline", nullable = false)
    private Instant submissionDeadline;
    
    @Column(name = "moderation_deadline", nullable = false)
    private Instant moderationDeadline;


    
    public ConferenceSettings(Instant submissionDeadline, Instant moderationDeadline) {

        this.id = (short) 1;
        changeDeadlines(submissionDeadline, moderationDeadline);
    }



    public void changeDeadlines(Instant submissionDeadline, Instant moderationDeadline) {

        if(submissionDeadline == null || moderationDeadline == null) {
            throw new IllegalArgumentException("Обязательны оба строка");
        }

        if(moderationDeadline.isBefore(submissionDeadline)) {
            throw new IllegalArgumentException("Срок модерации не может быть раньше срока подачи заявления");
        }

        this.submissionDeadline = submissionDeadline;
        this.moderationDeadline = moderationDeadline;
    }
}
