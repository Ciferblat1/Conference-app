package com.github.Ciferblat1.conference_app.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;



@Entity 
@Table(name = "applications")
@Getter 
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Application {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;


    @Embedded 
    private ApplicationAuthorSnapshot authorSnapshot;


    @ElementCollection 
    @CollectionTable(
        name = "application_supervisors",
        joinColumns = @JoinColumn(name = "application_id")
    )
    @OrderColumn(name = "supervisors_order")
    private List<ApplicationSupervisor> supervisors = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "direction_id", nullable = false)
    private Direction direction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pending_direction_id", nullable = true)
    private Direction pendingDirection;

    @Column(name = "title", nullable = false)
    private String title;


    @Column(name = "theses", nullable = false, columnDefinition = "text")
    private String theses;


    @ElementCollection 
    @CollectionTable(
        name = "application_references_list",
        joinColumns = @JoinColumn(name = "application_id")
    )
    @OrderColumn(name = "references_order")
    @Column(name = "reference_text", nullable = false, columnDefinition = "text")
    private List<String> referencesList = new ArrayList<>();


    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApplicationStatus status;

    @Column(name = "explanation", nullable = true)
    private String explanation;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;



    public Application(
        User user, 
        Direction direction, 
        ApplicationAuthorSnapshot authorSnapshot, 
        List<ApplicationSupervisor> supervisors,
        String title,
        String theses,
        List<String> referencesList,
        String explanation
    ) {

        this.author = user;
        this.direction = direction;
        this.authorSnapshot = authorSnapshot;
        this.status = ApplicationStatus.SUBMITTED;
        this.supervisors = new ArrayList<>(supervisors);
        this.referencesList = referencesList == null ? List.of() : new ArrayList<>(referencesList);
        this.title = title;
        this.theses = theses;
        this.explanation = explanation;
    }


    //Обновить заявку
    public void update(
        ApplicationAuthorSnapshot authorSnapshot, 
        List<ApplicationSupervisor> supervisors,
        String title,
        String theses,
        List<String> referencesList,
        ApplicationStatus newStatus
    ) {
        this.authorSnapshot = authorSnapshot;
        this.status = newStatus;
        this.supervisors = new ArrayList<>(supervisors);
        this.referencesList = referencesList == null ? List.of() : new ArrayList<>(referencesList);
        this.title = title;
        this.theses = theses;
        this.explanation = null;
    }

    //Подать запрос на перевод заявки 
    public void requestTransfer(Direction targetDirection) {
        this.pendingDirection = targetDirection;
    }

    //Одобрить перевод заявки
    public void applyTransfer() {

        Direction targetDirection = this.pendingDirection;
        if(targetDirection == null) {
            throw new IllegalStateException("Нельзя осуществить перевод заявки, если у нее нет целевого направления");
        }

        this.direction = targetDirection;
        this.pendingDirection = null;
    }

    //Отклонить перевод заявки
    public void rejectTransfer() {
        this.pendingDirection = null;
    }

    @PrePersist 
    private void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate 
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void withdraw() {
        this.status = ApplicationStatus.WITHDRAWN;
    }

    public void approve() {
        this.status = ApplicationStatus.APPROVED;
    }

    public void reject() {
        this.status = ApplicationStatus.REJECTED;
    }

    public void toRevision(String explanation) {
        this.status = ApplicationStatus.REVISION_REQUIRED;
        this.explanation = explanation;
    }

    public void submit() {
        this.status = ApplicationStatus.SUBMITTED;
    }
}
