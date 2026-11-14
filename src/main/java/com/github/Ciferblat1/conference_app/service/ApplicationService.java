package com.github.Ciferblat1.conference_app.service;

import org.springframework.security.access.AccessDeniedException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.Ciferblat1.conference_app.dto.ApplicationExpertResponse;
import com.github.Ciferblat1.conference_app.dto.ApplicationResponse;
import com.github.Ciferblat1.conference_app.dto.AuthorSnapshotResponse;
import com.github.Ciferblat1.conference_app.dto.SupervisorResponse;
import com.github.Ciferblat1.conference_app.entity.Application;
import com.github.Ciferblat1.conference_app.entity.ApplicationAuthorSnapshot;
import com.github.Ciferblat1.conference_app.entity.ApplicationStatus;
import com.github.Ciferblat1.conference_app.entity.ApplicationSupervisor;
import com.github.Ciferblat1.conference_app.entity.ConferenceSettings;
import com.github.Ciferblat1.conference_app.entity.Direction;
import com.github.Ciferblat1.conference_app.entity.ParticipantProfile;
import com.github.Ciferblat1.conference_app.entity.User;
import com.github.Ciferblat1.conference_app.entity.UserRole;
import com.github.Ciferblat1.conference_app.exception.ApplicationAccessDeniedException;
import com.github.Ciferblat1.conference_app.exception.ApplicationDeadlinePassedException;
import com.github.Ciferblat1.conference_app.exception.ApplicationNotFoundException;
import com.github.Ciferblat1.conference_app.exception.ConferenceSettingsNotFoundException;
import com.github.Ciferblat1.conference_app.exception.DirectionNotFoundException;
import com.github.Ciferblat1.conference_app.exception.InvalidApplicationStateException;
import com.github.Ciferblat1.conference_app.exception.UserNotFoundException;
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
    private final ParticipantProfileService participantProfileService;
    private final DirectionRepository directionRepository;
    private final ConferenceSettingsRepository conferenceSettingsRepository;


    @Transactional 
    public ApplicationResponse createApplication(
        UUID authorId, 
        UUID directionId, 
        List<ApplicationSupervisor> supervisors,
        String title, 
        String theses, 
        List<String> references

    ) {

        if(authorId == null) {
            throw new IllegalArgumentException("ID автора не может быть null");
        }

        if(directionId == null) {
            throw new IllegalArgumentException("ID направления не может быть null");
        }


        checkSubmissionDeadline();

        User author = userRepository.findById(authorId)
            .orElseThrow(() -> new UserNotFoundException(authorId));

        ApplicationAuthorSnapshot snapshot = createSnapshot(authorId);

        Direction direction = directionRepository.findById(directionId)
            .orElseThrow(() -> new DirectionNotFoundException(directionId));


        Application application = new Application(author, direction, snapshot, supervisors, title, theses, references, null);

        applicationRepository.save(application);
        return toResponse(application);
    }



    @Transactional(readOnly = true)
    public List<ApplicationResponse> getAllApplications(UUID userId) {

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        return applicationRepository
            .findByDirectionId(user.getDirectionId())
                .stream()
                    .map(application -> toResponse(application))
                        .toList();
    }


    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(UUID id, UUID userId) {

        Application application = findApplicationById(id, userId);

        return toResponse(application);
    }


    private Application findApplicationById(UUID id, UUID userId) {
        if(id == null || userId == null) {
            throw new IllegalArgumentException("ID не может быть null");
        }

        Application application = applicationRepository.findById(id).orElseThrow(() -> new ApplicationNotFoundException(id));

        checkUser(application, userId);
        
        return application;
    }



    @Transactional(readOnly = true)
    public List<ApplicationResponse> getApplicationsByAuthorId(UUID id) {

        if(id == null) {
            throw new IllegalArgumentException("ID не может быть null");
        }

        return applicationRepository
            .findByAuthorId(id)
                .stream()
                    .map(application -> toResponse(application))
                        .toList();
    }


    @Transactional 
    public List<ApplicationExpertResponse> getApplicationsForExpert(UUID expertId) {
        
        if(expertId == null) {
            throw new IllegalArgumentException("ID не может быть null");
        }

        User user = userRepository.findById(expertId).orElseThrow(() -> new UserNotFoundException(expertId));

        if(user.getRole() != UserRole.EXPERT) {
            throw new ApplicationAccessDeniedException(expertId);
        }

        return applicationRepository
            .findByDirectionId(user.getDirectionId())
                .stream()
                    .map(application -> toExpertResponse(application))
                        .toList();
    }



    @Transactional 
    public ApplicationResponse setWithdrawStatus(UUID id, UUID currentUserId) {

        if(id == null || currentUserId == null) {
            throw new IllegalArgumentException("ID не может быть null");
        }

        checkSubmissionDeadline();

        Application application = findApplicationById(id, currentUserId);

        checkApplicationTransferring(application);

        checkAuthor(application, currentUserId);

        if(application.getStatus() == ApplicationStatus.SUBMITTED || application.getStatus() == ApplicationStatus.CHANGED) {
            application.withdraw();
        }
        else {
            throw new InvalidApplicationStateException(id, application.getStatus());
        }
        
        return toResponse(application);
    }



    @Transactional 
    public ApplicationResponse updateApplication(
        UUID userId,
        UUID applicationId, 
        List<ApplicationSupervisor> supervisors,
        String title, 
        String theses, 
        List<String> references,
        boolean refreshAuthorSnapshot) {

            Application application = findApplicationById(applicationId, userId);

            if(application.getStatus() == ApplicationStatus.REVISION_REQUIRED) {
                checkModerationDeadline();
            }
            else {
                checkSubmissionDeadline();
            }

            checkApplicationTransferring(application);
            
            //Обновлять можно только поданные заявки
            ApplicationStatus oldStatus = application.getStatus();
            if(oldStatus == ApplicationStatus.APPROVED) {
                throw new InvalidApplicationStateException(applicationId, oldStatus);
            }

            checkAuthor(application, userId);
            
            ApplicationAuthorSnapshot snapshot = 
                refreshAuthorSnapshot ? createSnapshot(userId) : application.getAuthorSnapshot();

            ApplicationStatus newStatus = 
                oldStatus == ApplicationStatus.WITHDRAWN ? ApplicationStatus.WITHDRAWN : ApplicationStatus.CHANGED;

            application.update(snapshot, supervisors, title, theses, references, newStatus);

            return toResponse(application);
        }


    //===============================МОДЕРАЦИЯ ЗАЯВОК ОРГАНИЗАТОРОМ НАПРАВЛЕНИЯ===============================

    //Одобрить заявку организатором направления
    @Transactional 
    public ApplicationResponse setApprovedStatus(UUID id, UUID currentUserId) {

        if(id == null || currentUserId == null) {
            throw new IllegalArgumentException("ID не может быть null");
        }

        checkModerationDeadline();
        
        Application application = findApplicationById(id, currentUserId);

        checkApplicationTransferring(application);

        checkDirectionOrganizer(application.getDirection().getId(), currentUserId);

        if(application.getStatus() == ApplicationStatus.SUBMITTED || application.getStatus() == ApplicationStatus.CHANGED) {
            application.approve();
        }
        else {
            throw new InvalidApplicationStateException(id, application.getStatus());
        }
        
        return toResponse(application);
    }


    //Отклонить заявку организатором направления
    @Transactional 
    public ApplicationResponse setRejectedStatus(UUID id, UUID currentUserId) {

        if(id == null || currentUserId == null) {
            throw new IllegalArgumentException("ID не может быть null");
        }

        checkModerationDeadline();

        Application application = findApplicationById(id, currentUserId);

        checkApplicationTransferring(application);
        checkDirectionOrganizer(application.getDirection().getId(), currentUserId);

        if(application.getStatus() == ApplicationStatus.SUBMITTED || application.getStatus() == ApplicationStatus.CHANGED) {
            application.reject();
        }
        else {
            throw new InvalidApplicationStateException(id, application.getStatus());
        }
        
        return toResponse(application);
    }


    //Отправить заявку на доработку
    @Transactional 
    public ApplicationResponse setRevisionStatus(UUID id, UUID currentUserId, String explanation) {

        if(id == null || currentUserId == null) {
            throw new IllegalArgumentException("ID не может быть null");
        }

        checkModerationDeadline();

        Application application = findApplicationById(id, currentUserId);

        checkApplicationTransferring(application);
        checkDirectionOrganizer(application.getDirection().getId(), currentUserId);

        if(application.getStatus() == ApplicationStatus.SUBMITTED || application.getStatus() == ApplicationStatus.CHANGED) {
            application.toRevision(explanation);
        }
        else {
            throw new InvalidApplicationStateException(id, application.getStatus());
        }
        
        return toResponse(application);
    }


    //===============================ПЕРЕВОД ЗАЯВОК======================================

    //Запросить перевод заявки на новое направление
    @Transactional 
    public ApplicationResponse requestTransferApplication(UUID userId, UUID applicationId, UUID targetDirectionId) {

        if(userId == null || applicationId == null || targetDirectionId == null) {
            throw new IllegalArgumentException("Id пользвателя, заявки и целевого направления не могут быть пусты");
        }

        checkModerationDeadline();

        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new ApplicationNotFoundException(applicationId)); 

        checkDirectionOrganizer(application.getDirection().getId(), userId);

        if(targetDirectionId.equals(application.getDirection().getId())) {
            throw new IllegalArgumentException("Заява уже на целевом направлении");
        }

        if(application.getPendingDirection() != null) {
            throw new IllegalStateException("Заявка уже ожидает перевода");
        }

        ApplicationStatus status = application.getStatus();
        if(status != ApplicationStatus.SUBMITTED && status != ApplicationStatus.CHANGED) {
            throw new IllegalStateException("Заявку со статусом " + status + " нельзя перевести на другое направление");
        }

        Direction targetDirection = directionRepository.findById(targetDirectionId)
            .orElseThrow(() -> new DirectionNotFoundException(targetDirectionId));

        application.requestTransfer(targetDirection);
        return toResponse(application);
    }


    //Принять заявку на новое направление
    @Transactional 
    public ApplicationResponse applyTransferApplication(UUID userId, UUID applicationId) {

        if(userId == null || applicationId == null) {
            throw new IllegalArgumentException("Id пользвателя и заявки не могут быть пусты");
        }

        checkModerationDeadline();

        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new ApplicationNotFoundException(applicationId)); 

        if(application.getPendingDirection() == null) {
            throw new IllegalStateException("Нельзя перевести заявку, не ожидающую перевода");
        }

        checkDirectionOrganizer(application.getPendingDirection().getId(), userId);

        application.applyTransfer();
        return toResponse(application);
    }

    //Отклонить перевод заявки на новое направление
    @Transactional 
    public ApplicationResponse rejectTransferApplication(UUID userId, UUID applicationId) {

        if(userId == null || applicationId == null) {
            throw new IllegalArgumentException("Id пользвателя и заявки не могут быть пусты");
        }

        checkModerationDeadline();

        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new ApplicationNotFoundException(applicationId)); 

        if(application.getPendingDirection() == null) {
            throw new IllegalStateException("Нельзя отклонить заявку, не ожидающую перевода");
        }

        checkDirectionOrganizer(application.getPendingDirection().getId(), userId);

        application.rejectTransfer();
        return toResponse(application);
    }


    //Получить заявки, ожидающие от текущего пользователя подтверждения
    @Transactional 
    public List<ApplicationResponse> getPendingTransferApplications(UUID userId) {

        if(userId == null) {
            throw new IllegalArgumentException("Id пользователя не может быть пустым");
        }

        User currentUser = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        boolean isAccessAllowed = currentUser.getRole() == UserRole.ORGANIZER 
            && currentUser.getDirectionId() != null;

        if(!isAccessAllowed) {
            throw new IllegalArgumentException("Переданный id не принадлежит организатору направления");
        }

        return applicationRepository
            .findByPendingDirection_Id(currentUser.getDirectionId())
                .stream()
                    .map(application -> toResponse(application))
                        .toList();
    }

    //===============================ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ===============================


    //Преобразовать сущность заявки в ответ заявки
    private ApplicationResponse toResponse(Application application) {
        return new ApplicationResponse(
            application.getId(),
            application.getAuthor().getId(),
            application.getDirection().getId(),
            toAuthorSnapshotResponse(application.getAuthorSnapshot()),
            application.getSupervisors().stream()
                    .map(this::toSupervisorResponse)
                    .toList(),
            application.getTitle(),
            application.getTheses(),
            List.copyOf(application.getReferencesList()),
            application.getStatus(),
            application.getCreatedAt(),
            application.getUpdatedAt(),
            application.getExplanation(),
            application.getPendingDirection() != null ? application.getPendingDirection().getId() : null
        );
    }

    //Преобразовать сущность заявки в ответ заявки для эксперта (без личных данных)
    private ApplicationExpertResponse toExpertResponse(Application application) {
        return new ApplicationExpertResponse(
            application.getId(),
            application.getDirection().getId(),
            application.getTitle(),
            application.getTheses(),
            List.copyOf(application.getReferencesList()),
            application.getStatus(),
            application.getCreatedAt(),
            application.getUpdatedAt()
        );
    }

    //Преобразовать снапшот данных автора в ответ снапшота
    private AuthorSnapshotResponse toAuthorSnapshotResponse(
            ApplicationAuthorSnapshot snapshot
    ) {
        return new AuthorSnapshotResponse(
                snapshot.getFirstName(),
                snapshot.getSurname(),
                snapshot.getPatronymic(),
                snapshot.getPhone(),
                snapshot.getGroupName(),
                snapshot.getEmail(),
                snapshot.getFundingType()
        );
    }


    //Преобразовать сущность научного руководителя в ответ руководителя
    private SupervisorResponse toSupervisorResponse(
            ApplicationSupervisor supervisor
    ) {
        return new SupervisorResponse(
                supervisor.getFirstName(),
                supervisor.getSurname(),
                supervisor.getPatronymic(),
                supervisor.getPosition(),
                supervisor.getDepartment()
        );
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


    //Проверяет, прошел ли момент дедлайна модерации заявки
    private void checkModerationDeadline() {

        Short settingsId = (short) 1;
        
        ConferenceSettings settings = conferenceSettingsRepository.findById(settingsId)
            .orElseThrow(() -> new ConferenceSettingsNotFoundException(settingsId));

        if(!Instant.now().isBefore(settings.getModerationDeadline())) {
            throw new ApplicationDeadlinePassedException();
        }
    }


    //Получить по ID пользователя его личную информацию
    private ApplicationAuthorSnapshot createSnapshot(UUID userId) {

        User author = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        ParticipantProfile profile = participantProfileService.getMyProfile(userId);

        return new ApplicationAuthorSnapshot(
            profile.getFirstName(),
            profile.getSurname(),
            profile.getPatronymic(),
            profile.getPhone(),
            profile.getGroupName(),
            author.getEmail(),
            profile.getFundingType()
            );
    }


    //Проверить, является ли пользователь автором заявки
    private void checkAuthor(Application application, UUID currentId) {

        boolean isAuthor = application.getAuthor().getId().equals(currentId);

        if(!isAuthor) {
            throw new ApplicationAccessDeniedException(application.getId());
        }
    }


    //Проверить, является ли пользователь организатором направления
    private void checkDirectionOrganizer(UUID directionId, UUID userId) {

        if(!isOrganizerForDirection(directionId, userId)) {
            throw new AccessDeniedException("Пользователь с id "+ userId + " не является организатором направления с id" + directionId);
        }
    }

    private boolean isOrganizerForDirection(UUID directionId, UUID userId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        return user.getRole() == UserRole.ORGANIZER 
            && user.getDirectionId().equals(directionId);
    }

    //Проверить, является ли пользователь автором заявки или организатором направления
    private void checkUser(Application application, UUID currentId) {

        boolean isAuthor = application.getAuthor().getId().equals(currentId);

        if(!isAuthor && !isOrganizerForDirection(application.getDirection().getId(), currentId)) {
            throw new ApplicationAccessDeniedException(application.getId());
        }
    }

    private void checkApplicationTransferring(Application application) {
        if(application.getPendingDirection() != null) {
            throw new IllegalStateException("Заявка находится в ожидании перевода на другое направление");
        }
    }
}
