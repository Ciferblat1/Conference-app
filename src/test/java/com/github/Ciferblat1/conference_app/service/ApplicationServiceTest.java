package com.github.Ciferblat1.conference_app.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.github.Ciferblat1.conference_app.entity.Application;
import com.github.Ciferblat1.conference_app.entity.ApplicationDocument;
import com.github.Ciferblat1.conference_app.entity.ApplicationStatus;
import com.github.Ciferblat1.conference_app.entity.ConferenceSettings;
import com.github.Ciferblat1.conference_app.entity.User;
import com.github.Ciferblat1.conference_app.exception.ApplicationDeadlinePassedException;
import com.github.Ciferblat1.conference_app.exception.ConferenceSettingsNotFoundException;
import com.github.Ciferblat1.conference_app.exception.DirectionNotFoundException;
import com.github.Ciferblat1.conference_app.exception.UserNotFoundException;
import com.github.Ciferblat1.conference_app.entity.Direction;
import com.github.Ciferblat1.conference_app.repository.ApplicationDocumentRepository;
import com.github.Ciferblat1.conference_app.repository.ApplicationRepository;
import com.github.Ciferblat1.conference_app.repository.ConferenceSettingsRepository;
import com.github.Ciferblat1.conference_app.repository.DirectionRepository;
import com.github.Ciferblat1.conference_app.repository.UserRepository;

@ExtendWith (MockitoExtension.class)
public class ApplicationServiceTest {
    
    @Mock 
    private ApplicationRepository applicationRepository;

    @Mock 
    private UserRepository userRepository;

    @Mock 
    private DirectionRepository directionRepository;

    @Mock 
    private ConferenceSettingsRepository conferenceSettingsRepository;

    @Mock 
    private ApplicationDocumentRepository applicationDocumentRepository;

    @InjectMocks 
    private ApplicationService applicationService;


    /*
    ------------------------------АСПЕКТЫ------------------------------
    
    1. Время создания заявки: до дедлайна, после дедлайна
    2. Корректность входных данных: корректны, некорректны
    3. Автор: переданный автор есть передан несуществующий
    4. Направление: переданное направление есть, передано несуществующее
    5. Настройки конференции отсутствуют
    6. Статус заявки: корректен для выполняемой операции, некорректен
    */


    @Test 
    void createApplicationBeforeExpiredDeadline() {

        configureDeadline(false);

        UUID userId = UUID.randomUUID();
        UUID directionId = UUID.randomUUID();

        User user = mock(User.class);
        Direction direction = mock(Direction.class);

        when(userRepository.findById(userId))
            .thenReturn(Optional.of(user));

        when(directionRepository.findById(directionId))
            .thenReturn(Optional.of(direction));

        MockMultipartFile document = validFile();

        //Act
        Application application = applicationService.createApplication(userId, directionId, document);

        //Asserts
        verify(applicationRepository).save(application);

        assertSame(user, application.getAuthor());
        assertSame(direction, application.getDirection());
        assertEquals(ApplicationStatus.SUBMITTED, application.getStatus());


        ArgumentCaptor<ApplicationDocument> captor = ArgumentCaptor.forClass(ApplicationDocument.class);
        verify(applicationDocumentRepository).save(captor.capture());

        ApplicationDocument doc = captor.getValue();

        assertSame(application, doc.getApplication());
        assertEquals("application.docx", doc.getOriginalFilename());
        assertEquals("application/vnd.openxmlformats-officedocument.wordprocessingml.document", doc.getContentType());
        assertArrayEquals(doc.getContent(), doc.getContent());
    }


    @Test 
    void tryCreateApplicationAfterDeadline() {

        configureDeadline(true);

        UUID userId = UUID.randomUUID();
        UUID directionId = UUID.randomUUID();

        MockMultipartFile document = validFile();


        //Act
        assertThrows(ApplicationDeadlinePassedException.class, 
            () -> applicationService.createApplication(
                userId, 
                directionId, 
                document)
            );

        //Asserts
        verifyNoInteractions(
            applicationRepository,
            applicationDocumentRepository,
            userRepository,
            directionRepository
        );
    }


    //Тест запускается трижды для проверки создания, обновления и отзыва заявки
    @ParameterizedTest
    @EnumSource(Operation.class)
    void modifyingOperationRejectsExpiredDeadline(Operation operation) {
        configureDeadline(true);

        assertThrows(
            ApplicationDeadlinePassedException.class,
            () -> performOperation(
                    operation,
                    UUID.randomUUID(),
                    validFile()
            )
        );

        verifyNoInteractions(
            applicationRepository,
            applicationDocumentRepository,
            userRepository,
            directionRepository
        );
    }

    
    //Некорректные входные данные
    @ParameterizedTest
    @EnumSource(Operation.class)
    void modifyingOperationRejectsNullId(Operation operation) {
        assertThrows(
                IllegalArgumentException.class,
                () -> performOperation(operation, null, validFile())
        );

        verifyNoInteractions(
                conferenceSettingsRepository,
                applicationRepository,
                applicationDocumentRepository,
                userRepository,
                directionRepository
        );
    }

    
    //Отсутствующее направление при создании заявки
    @Test
    void createApplicationRejectsNullDirectionId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> applicationService.createApplication(
                        UUID.randomUUID(),
                        null,
                        validFile()
                )
        );

        verifyNoInteractions(
                conferenceSettingsRepository,
                applicationRepository,
                applicationDocumentRepository,
                userRepository,
                directionRepository
        );
    }


    //Несуществующий автор при создании заявки
    @Test
    void createApplicationRejectsMissingAuthor() {
        configureDeadline(false);

        UUID authorId = UUID.randomUUID();

        when(userRepository.findById(authorId))
                .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> applicationService.createApplication(
                        authorId,
                        UUID.randomUUID(),
                        validFile()
                )
        );

        verifyNoInteractions(
                directionRepository,
                applicationRepository,
                applicationDocumentRepository
        );
    }


    //Несуществующее направление при создании заявки
    @Test
    void createApplicationRejectsMissingDirection() {
        configureDeadline(false);

        UUID authorId = UUID.randomUUID();
        UUID directionId = UUID.randomUUID();

        when(userRepository.findById(authorId))
                .thenReturn(Optional.of(mock(User.class)));

        when(directionRepository.findById(directionId))
                .thenReturn(Optional.empty());

        assertThrows(
                DirectionNotFoundException.class,
                () -> applicationService.createApplication(
                        authorId,
                        directionId,
                        validFile()
                )
        );

        verifyNoInteractions(
                applicationRepository,
                applicationDocumentRepository
        );
    }


    //Отсутствующие дедлайны
    @ParameterizedTest
    @EnumSource(Operation.class)
    void modifyingOperationRejectsMissingSettings(Operation operation) {
        when(conferenceSettingsRepository.findById((short) 1))
                .thenReturn(Optional.empty());

        assertThrows(
                ConferenceSettingsNotFoundException.class,
                () -> performOperation(
                        operation,
                        UUID.randomUUID(),
                        validFile()
                )
        );

        verifyNoInteractions(
                applicationRepository,
                applicationDocumentRepository,
                userRepository,
                directionRepository
        );
    }


    //Успешный отзыв заявки
    @Test
    void withdrawSubmittedApplicationChangesStatus() {
        configureDeadline(false);

        UUID id = UUID.randomUUID();
        Application application = createMockApplication();

        when(applicationRepository.findById(id))
                .thenReturn(Optional.of(application));

        Application result = applicationService.setWithdrawStatus(id);

        assertSame(application, result);
        assertEquals(ApplicationStatus.WITHDRAWN, result.getStatus());

        verify(applicationRepository, never()).deleteById(any());
        verifyNoInteractions(applicationDocumentRepository);
    }


    //Успешная замена документа и повторная подача заявки
    @Test
    void updateWithdrawnApplicationReplacesDocumentAndSubmitsApplication() {
        configureDeadline(false);

        UUID id = UUID.randomUUID();

        Application application = createMockApplication();
        application.withdraw();

        LocalDateTime previousCreatedAt = application.getCreatedAt();

        ApplicationDocument document = new ApplicationDocument(
                application,
                "old.doc",
                "application/msword",
                new byte[] {9, 8, 7}
        );

        when(applicationRepository.findById(id))
                .thenReturn(Optional.of(application));

        when(applicationDocumentRepository.findById(id))
                .thenReturn(Optional.of(document));

        MockMultipartFile newFile = validFile();

        Application result = applicationService.updateApplicationDocument(
                id,
                newFile
        );

        assertSame(application, result);
        assertSame(application, document.getApplication());

        assertEquals("application.docx", document.getOriginalFilename());
        assertEquals(
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                document.getContentType()
        );
        assertArrayEquals(new byte[] {1, 2, 3}, document.getContent());

        assertEquals(ApplicationStatus.SUBMITTED, application.getStatus());
        assertEquals(previousCreatedAt, application.getCreatedAt());
        assertNotNull(application.getUpdatedAt());

        verify(applicationDocumentRepository, never())
                .save(any(ApplicationDocument.class));
    }
    

    //Вспомогательные методы 

    /**
     * Создает валидный файл для заявки
     * 
     * @return корректный файл 
     */
    private MockMultipartFile validFile() {
        return new MockMultipartFile(
            "file",
            "application.docx",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            new byte[] {1, 2, 3}
        );
    }
    

    /**
     * Устанавливает дедлайны T1 и T2
     * 
     * @param expired - прошёл ли дедлайн
     */
    private void configureDeadline(boolean expired) {
        ConferenceSettings settings = new ConferenceSettings(
            Instant.parse(expired
                ? "2000-01-01T00:00:00Z"
                : "2100-01-01T00:00:00Z"),
            Instant.parse(expired
                ? "2000-02-01T00:00:00Z"
                : "2100-02-01T00:00:00Z")
        );

        when(conferenceSettingsRepository.findById((short) 1))
            .thenReturn(Optional.of(settings));
    }


    private Application createMockApplication() {
        return new Application(mock(User.class), mock(Direction.class));
    }

    private enum Operation {
        CREATE,
        UPDATE,
        WITHDRAW
    }

    private void performOperation(
        Operation operation,
        UUID id,
        MultipartFile file
    ) {
        switch (operation) {
            case CREATE -> applicationService.createApplication(
                id,
                UUID.randomUUID(),
                file
            );
            case UPDATE -> applicationService.updateApplicationDocument(id, file);
            case WITHDRAW -> applicationService.setWithdrawStatus(id);
        }
    }
}
