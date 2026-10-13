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


    /**
    * Создаёт поданную заявку со статусом {@code SUBMITTED}
    * и сохраняет её документ в одной транзакции.
    * Создание разрешено строго до срока Т1.
    *
    * <p>Файл должен быть непустым, иметь расширение {@code .doc}
    * или {@code .docx} и размер не более 10 МБ</p>
    *
    * @param authorId идентификатор существующего автора заявки
    * @param directionId идентификатор существующего направления
    * @param file загружаемый документ заявки
    * @throws IllegalArgumentException если идентификаторы отсутствуют,
    *         файл отсутствует или пуст, превышает допустимый размер,
    *         имеет недопустимое имя или расширение
    * @throws ConferenceSettingsNotFoundException если настройки конференции отсутствуют
    * @throws ApplicationDeadlinePassedException если наступил срок Т1
    * @throws UserNotFoundException если автор не найден
    * @throws DirectionNotFoundException если направление не найдено
    * @throws UncheckedIOException если не удалось прочитать содержимое файла
    * @return созданная заявка
    */
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


    /**
    * Заменяет документ заявки без сохранения предыдущей версии.
    * Разрешено до Т1 для отозванных заявок. Статус заявки при замене не изменяется.
    *
    * @param id идентификатор заявки
    * @param updatedFile новый документ в формате DOC или DOCX, до 10 МиБ
    * @throws ApplicationNotFoundException если заявка не найдена
    * @throws DocumentNotFoundException если документ заявки не найден
    * @throws ApplicationDeadlinePassedException если наступил срок Т1
    * @throws InvalidApplicationStateException если статус запрещает замену
    * @return заявка после замены документа
    */
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

        if( application.getStatus() == ApplicationStatus.WITHDRAWN) 
        {
            byte[] content = readFileContent(updatedFile);
            document.updateDocument(updatedFile.getOriginalFilename(), contentType, content);
            application.submit();
            application.onUpdate();
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



    /**
     * Проверяет, прошел ли момент дедлайна подачи заявки
     * 
     * @throws ApplicationDeadlinePassedException если дедлайн уже прошел
     */
    private void checkSubmissionDeadline() {

        Short settingsId = (short) 1;
        
        ConferenceSettings settings = conferenceSettingsRepository.findById(settingsId)
            .orElseThrow(() -> new ConferenceSettingsNotFoundException(settingsId));

        if(!Instant.now().isBefore(settings.getSubmissionDeadline())) {
            throw new ApplicationDeadlinePassedException();
        }
    }


    /**
     * Проверяет файл на корректность
     * 
     * @param file проверяемый файл
     * @throws IllegalArgumentException если файл пуст
     * @throws IllegalArgumentException если размер файла превышает 10МБ
     * @throws IllegalArgumentException если имя файла отсутствует или длиннее 255 символов
     * @throws IllegalArgumentException если тип файла не .doc или .docx
     * @return {@code true}, если файл корректен, {@code false} иначе
     */
    private String checkFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл заявки обязателен");
        }

        long maxFileSize = 10L * 1024 * 1024;

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("Размер файла не должен превышать 10 МБ");
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


    /**
     * Считывает данные из файла
     * 
     * @param file читаемый файл
     * @throws UncheckedIOException если не удалось прочитать файл
     * @return данные файла
     */
    private byte[] readFileContent(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new UncheckedIOException("Не удалось прочитать файл", exception);
        }
    }
}
