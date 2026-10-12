package com.github.Ciferblat1.conference_app.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.github.Ciferblat1.conference_app.entity.Application;
import com.github.Ciferblat1.conference_app.entity.ApplicationDocument;
import com.github.Ciferblat1.conference_app.entity.ApplicationStatus;
import com.github.Ciferblat1.conference_app.entity.ConferenceSettings;
import com.github.Ciferblat1.conference_app.entity.Direction;
import com.github.Ciferblat1.conference_app.entity.User;
import com.github.Ciferblat1.conference_app.exception.ApplicationDeadlinePassedException;
import com.github.Ciferblat1.conference_app.exception.ApplicationNotFoundException;
import com.github.Ciferblat1.conference_app.exception.ConferenceSettingsNotFoundException;
import com.github.Ciferblat1.conference_app.exception.DirectionNotFoundException;
import com.github.Ciferblat1.conference_app.exception.DocumentNotFoundException;
import com.github.Ciferblat1.conference_app.exception.InvalidApplicationStateException;
import com.github.Ciferblat1.conference_app.exception.UserNotFoundException;
import com.github.Ciferblat1.conference_app.repository.ApplicationDocumentRepository;
import com.github.Ciferblat1.conference_app.repository.ApplicationRepository;
import com.github.Ciferblat1.conference_app.repository.ConferenceSettingsRepository;
import com.github.Ciferblat1.conference_app.repository.DirectionRepository;
import com.github.Ciferblat1.conference_app.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final DirectionRepository directionRepository;
    private final ConferenceSettingsRepository conferenceSettingsRepository;
    private final ApplicationDocumentRepository applicationDocumentRepository;

    @Transactional 
    public Application createApplication(UUID authorId, UUID directionId, MultipartFile file) {

        if(authorId == null) {
            throw new IllegalArgumentException("ID автора не может быть null");
        }

        if(directionId == null) {
            throw new IllegalArgumentException("ID направления не может быть null");
        }

        String contentType = checkFile(file);

        checkSubmissionDeadline();

        User author = userRepository.findById(authorId)
            .orElseThrow(() -> new UserNotFoundException(authorId));

        Direction direction = directionRepository.findById(directionId)
            .orElseThrow(() -> new DirectionNotFoundException(directionId));

        byte[] content = readFileContent(file);

        Application application = new Application(author, direction);
        ApplicationDocument document = new ApplicationDocument(application, file.getOriginalFilename(), contentType, content);

        applicationRepository.save(application);
        applicationDocumentRepository.save(document);
        return application;
    }



    @Transactional(readOnly = true)
    public List<Application> getAllApplications() {

        return  applicationRepository.findAll();

    }


    @Transactional(readOnly = true)
    public Application getApplicationById(UUID id) {

        if(id == null) {
            throw new IllegalArgumentException("ID не может быть null");
        }

        
        return applicationRepository.findById(id).orElseThrow(() -> new ApplicationNotFoundException(id));
    }


    @Transactional 
    public Application updateApplicationDocument(UUID id, MultipartFile updatedFile) {

        if(id == null) {
            throw new IllegalArgumentException("ID не может быть null");
        }

        String contentType = checkFile(updatedFile);
        checkSubmissionDeadline();

        Application application = getApplicationById(id);
        ApplicationDocument document = applicationDocumentRepository.findById(id)
            .orElseThrow(() -> new DocumentNotFoundException(id));

        if(application.getStatus() == ApplicationStatus.SUBMITTED || 
            application.getStatus() == ApplicationStatus.WITHDRAWN) 
        {
            byte[] content = readFileContent(updatedFile);
            document.updateDocument(updatedFile.getOriginalFilename(), contentType, content);
            application.markDocumentUpdated();
        }
        else {
            throw new InvalidApplicationStateException(id, application.getStatus());
        }

        return application;
    }


    @Transactional 
    public Application setWithdrawStatus(UUID id) {

        if(id == null) {
            throw new IllegalArgumentException("ID не может быть null");
        }

        checkSubmissionDeadline();

        Application application = getApplicationById(id);

        if(application.getStatus() == ApplicationStatus.SUBMITTED) {
            application.withdraw();
        }
        else {
            throw new InvalidApplicationStateException(id, application.getStatus());
        }
        
        return application;
    }



    private void checkSubmissionDeadline() {

        Short settingsId = (short) 1;
        
        ConferenceSettings settings = conferenceSettingsRepository.findById(settingsId)
            .orElseThrow(() -> new ConferenceSettingsNotFoundException(settingsId));

        if(!Instant.now().isBefore(settings.getSubmissionDeadline())) {
            throw new ApplicationDeadlinePassedException();
        }
    }


    private String checkFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл заявки обязателен");
        }

        long maxFileSize = 10L * 1024 * 1024;

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("Размер файла не должен превышать 10 МиБ");
        }

        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null
            || originalFilename.isBlank()
            || originalFilename.length() > 255) 
        {
            throw new IllegalArgumentException("Недопустимое имя файла");
        }

        String lowerFilename = originalFilename.toLowerCase(Locale.ROOT);
        String contentType;

        if(lowerFilename.endsWith(".docx")) {
            contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } 
        else if(lowerFilename.endsWith(".doc")) {
            contentType = "application/msword";
        } 
        else {
            throw new IllegalArgumentException("Разрешены только файлы .doc и .docx");
        }

        return contentType;
    }


    private byte[] readFileContent(MultipartFile file) {
    try {
        return file.getBytes();
    } catch (IOException exception) {
        throw new UncheckedIOException(
                "Не удалось прочитать файл",
                exception
        );
    }
}
}
