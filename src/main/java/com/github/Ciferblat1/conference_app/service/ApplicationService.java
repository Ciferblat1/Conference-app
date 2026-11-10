package com.github.Ciferblat1.conference_app.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Application createApplication(
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
        return application;
    }



    @Transactional(readOnly = true)
    public List<Application> getAllApplications(UUID userId) {

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        return  applicationRepository.findByDirectionId(user.getDirectionId());
    }


    @Transactional(readOnly = true)
    public Application getApplicationById(UUID id, UUID userId) {

        if(id == null || userId == null) {
            throw new IllegalArgumentException("ID не может быть null");
        }

        Application application = applicationRepository.findById(id).orElseThrow(() -> new ApplicationNotFoundException(id));

        checkUser(application, userId);
        
        return application;
    }



    @Transactional(readOnly = true)
    public List<Application> getApplicationsByAuthorId(UUID id) {

        if(id == null) {
            throw new IllegalArgumentException("ID не может быть null");
        }

        return applicationRepository.findByAuthorId(id);
    }


    @Transactional 
    public List<Application> getApplicationsForExpert(UUID expertId) {
        
        if(expertId == null) {
            throw new IllegalArgumentException("ID не может быть null");
        }

        User user = userRepository.findById(expertId).orElseThrow(() -> new UserNotFoundException(expertId));

        if(user.getRole() != UserRole.EXPERT) {
            throw new ApplicationAccessDeniedException(expertId);
        }

        return applicationRepository.findByDirectionId(user.getDirectionId());
    }



    @Transactional 
    public Application setWithdrawStatus(UUID id, UUID currentUserId) {

        if(id == null || currentUserId == null) {
            throw new IllegalArgumentException("ID не может быть null");
        }

        checkSubmissionDeadline();

        Application application = getApplicationById(id, currentUserId);

        checkAuthor(application, currentUserId);

        if(application.getStatus() == ApplicationStatus.SUBMITTED || application.getStatus() == ApplicationStatus.CHANGED) {
            application.withdraw();
        }
        else {
            throw new InvalidApplicationStateException(id, application.getStatus());
        }
        
        return application;
    }



    @Transactional 
    public Application updateApplication(
        UUID userId,
        UUID applicationId, 
        List<ApplicationSupervisor> supervisors,
        String title, 
        String theses, 
        List<String> references,
        boolean refreshAuthorSnapshot) {

            Application application = getApplicationById(applicationId, userId);

            if(application.getStatus() == ApplicationStatus.REVISION_REQUIRED) {
                checkModerationDeadline();
            }
            else {
                checkSubmissionDeadline();
            }
            
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

            return application;
        }


    //===============================МОДЕРАЦИЯ ЗАЯВОК ОРГАНИЗАТОРОМ НАПРАВЛЕНИЯ===============================

    //Одобрить заявку организатором направления
    @Transactional 
    public Application setApprovedStatus(UUID id, UUID currentUserId) {

        if(id == null || currentUserId == null) {
            throw new IllegalArgumentException("ID не может быть null");
        }

        checkModerationDeadline();

        Application application = getApplicationById(id, currentUserId);

        checkDirectionOrganizer(application, currentUserId);

        if(application.getStatus() == ApplicationStatus.SUBMITTED || application.getStatus() == ApplicationStatus.CHANGED) {
            application.approve();
        }
        else {
            throw new InvalidApplicationStateException(id, application.getStatus());
        }
        
        return application;
    }


    //Отклонить заявку организатором направления
    @Transactional 
    public Application setRejectedStatus(UUID id, UUID currentUserId) {

        if(id == null || currentUserId == null) {
            throw new IllegalArgumentException("ID не может быть null");
        }

        checkModerationDeadline();

        Application application = getApplicationById(id, currentUserId);

        checkDirectionOrganizer(application, currentUserId);

        if(application.getStatus() == ApplicationStatus.SUBMITTED || application.getStatus() == ApplicationStatus.CHANGED) {
            application.reject();
        }
        else {
            throw new InvalidApplicationStateException(id, application.getStatus());
        }
        
        return application;
    }


    //Отправить заявку на доработку
    @Transactional 
    public Application setRevisionStatus(UUID id, UUID currentUserId, String explanation) {

        if(id == null || currentUserId == null) {
            throw new IllegalArgumentException("ID не может быть null");
        }

        checkModerationDeadline();

        Application application = getApplicationById(id, currentUserId);

        checkDirectionOrganizer(application, currentUserId);

        if(application.getStatus() == ApplicationStatus.SUBMITTED || application.getStatus() == ApplicationStatus.CHANGED) {
            application.toRevision(explanation);
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
    private void checkDirectionOrganizer(Application application, UUID currentId) {

        User user = userRepository.findById(currentId).orElseThrow(() -> new UserNotFoundException(currentId));

        boolean isOrganizerForDirection = 
            user.getRole() == UserRole.ORGANIZER 
            && user.getDirectionId().equals(application.getDirection().getId());

        if(!isOrganizerForDirection) {
            throw new ApplicationAccessDeniedException(application.getId());
        }
    }


    //Проверить, является ли пользователь автором заявки или организатором направления
    private void checkUser(Application application, UUID currentId) {

        boolean isAuthor = application.getAuthor().getId().equals(currentId);

        User user = userRepository.findById(currentId).orElseThrow(() -> new UserNotFoundException(currentId));

        boolean isOrganizerForDirection = 
            user.getRole() == UserRole.ORGANIZER 
            && user.getDirectionId().equals(application.getDirection().getId());


        if(!isAuthor && !isOrganizerForDirection) {
            throw new ApplicationAccessDeniedException(application.getId());
        }
    }
}
