package com.github.Ciferblat1.conference_app.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity 
@Getter 
@NoArgsConstructor (access = AccessLevel.PROTECTED)
@Table (name = "application_documents")
public class ApplicationDocument {

    @Id 
    @Column (name = "application_id")
    private UUID id;


    @MapsId 
    @OneToOne (fetch = FetchType.LAZY, optional = false)
    @JoinColumn (name = "application_id", nullable = false)
    private Application application;

    @Column (name = "original_filename", nullable = false)
    private String originalFilename;

    @Column (name = "content_type", nullable = false)
    private String contentType;

    @Column (name = "content", nullable = false, columnDefinition = "bytea")
    private byte[] content;

    public ApplicationDocument(
        Application application,
        String originalFilename,
        String contentType,
        byte[] content
    ) {

        this.application = application;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.content = content;
    }


    public void updateDocument(String originalFilename, String contentType, byte[] content) {
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.content = content;
    }
}
