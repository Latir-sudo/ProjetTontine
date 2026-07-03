package com.tontineApp.tontine_manager.service;

import com.tontineApp.tontine_manager.dto.NotificationRequest;
import com.tontineApp.tontine_manager.dto.NotificationResponse;
import com.tontineApp.tontine_manager.enumeration.StatutNotification;
import com.tontineApp.tontine_manager.exception.RessourceNotFoundException;
import com.tontineApp.tontine_manager.mapper.NotificationMapper;
import com.tontineApp.tontine_manager.model.Membre;
import com.tontineApp.tontine_manager.model.Notification;
import com.tontineApp.tontine_manager.repository.MembreRepository;
import com.tontineApp.tontine_manager.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final MembreRepository membreRepository;

    public NotificationResponse creerNotification(NotificationRequest notificationRequest) {
        Membre membre = getMembre(notificationRequest.getIdUser(), notificationRequest.getIdTontine());

        Notification notification = notificationMapper.toNotification(notificationRequest);
        notification.setMembre(membre);
        notification.setDateCreation(LocalDateTime.now());

        if (notification.getStatutNotification() == null) {
            notification.setStatutNotification(Boolean.TRUE.equals(notification.getEstLu())
                    ? StatutNotification.LU
                    : StatutNotification.NON_LU);
        }

        Notification savedNotification = notificationRepository.save(notification);
        return notificationMapper.toNotificationResponse(savedNotification);
    }

    public void supprimerNotification(Integer id) {
        if (!notificationRepository.existsById(id)) {
            throw new RessourceNotFoundException("Notification " + id + " non trouvee");
        }

        notificationRepository.deleteById(id);
    }

    public List<NotificationResponse> getNotificationMembres(Integer idUser, Integer idTontine) {
        Membre membre = getMembre(idUser, idTontine);

        return notificationRepository.findByMembre_Id(membre.getId()).stream()
                .map(notificationMapper::toNotificationResponse)
                .toList();
    }

    public List<NotificationResponse> getNotificationNonLues(Integer idUser, Integer idTontine) {
        Membre membre = getMembre(idUser, idTontine);

        return notificationRepository.findByMembre_IdAndEstLu(membre.getId(), false).stream()
                .map(notificationMapper::toNotificationResponse)
                .toList();
    }

    public long compterNotificationsNonLues(Integer idUser, Integer idTontine) {
        Membre membre = getMembre(idUser, idTontine);
        return notificationRepository.findByMembre_IdAndEstLu(membre.getId(), false).size();
    }

    public void supprimerNotificationMembre(Integer idUser, Integer idTontine) {
        Membre membre = getMembre(idUser, idTontine);
        notificationRepository.deleteByMembre_Id(membre.getId());
    }

    public NotificationResponse marquerLue(Integer idNotification, Boolean valeur) {
        Notification notification = notificationRepository.findById(idNotification)
                .orElseThrow(() -> new RessourceNotFoundException("Notification " + idNotification + " non trouvee"));

        boolean estLu = Boolean.TRUE.equals(valeur);
        notification.setEstLu(estLu);
        notification.setStatutNotification(estLu ? StatutNotification.LU : StatutNotification.NON_LU);

        Notification savedNotification = notificationRepository.save(notification);
        return notificationMapper.toNotificationResponse(savedNotification);
    }

    public void marquerToutesNotificationsCommeLues(Integer idUser, Integer idTontine) {
        Membre membre = getMembre(idUser, idTontine);
        List<Notification> notificationsNonLues = notificationRepository.findByMembre_IdAndEstLu(membre.getId(), false);

        for (Notification notification : notificationsNonLues) {
            notification.setEstLu(true);
            notification.setStatutNotification(StatutNotification.LU);
        }

        notificationRepository.saveAll(notificationsNonLues);
    }

    private Membre getMembre(Integer idUser, Integer idTontine) {
        return membreRepository.findByTontine_IdAndUser_Id(idTontine, idUser)
                .orElseThrow(() -> new RessourceNotFoundException(
                        "L'utilisateur avec l'id " + idUser + " n'est pas membre de la tontine avec l'id " + idTontine
                ));
    }
}
