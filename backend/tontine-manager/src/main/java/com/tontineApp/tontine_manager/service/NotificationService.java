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

        final Membre membre = membreRepository.findByTontine_IdAndUser_Id(notificationRequest.getIdTontine(), notificationRequest.getIdUser())
                .orElseThrow(() -> new RessourceNotFoundException("L'utilisateur avec l'id " + notificationRequest.getIdUser() + " n'est pas membre de la tontine avec l'id " + notificationRequest.getIdTontine()));

        System.out.println("test si cette méthode se trouve le problème");

            Notification notification = notificationMapper.toNotification(notificationRequest);
            notification.setMembre(membre);
            notification.setDateCreation(LocalDateTime.now());
            Notification savedNotification = notificationRepository.save(notification);
            return notificationMapper.toNotificationResponse(savedNotification);

    }

    public void supprimerNotification(Integer id) {
        notificationRepository.deleteById(id);
    }

    public List<NotificationResponse> getNotificationMembres(Integer idUser,Integer idTontine) {
        Membre membre = membreRepository.findByTontine_IdAndUser_Id(idTontine,idUser).orElseThrow(()->new RessourceNotFoundException("l'utilisateu "+idUser+" n'est pas membre de la tontine "+idTontine));
        return notificationRepository.findByMembre_Id(membre.getId()).stream()
                .map(notificationMapper::toNotificationResponse)
                .toList();
    }

    // récupérer les notifications non lues de l'utilisateur dans une tontine

    public List<NotificationResponse> getNotificationNonLues(Integer idUser,Integer idTontine) {
        Membre membre = membreRepository.findByTontine_IdAndUser_Id(idTontine,idUser).orElseThrow(()->new RessourceNotFoundException("l'utilisateu "+idUser+" n'est pas membre de la tontine "+idTontine));
        return notificationRepository.findByMembre_IdAndEstLu(membre.getId(),false).stream()
                .map(notificationMapper::toNotificationResponse)
                .toList();
    }

    // supprimer toutes les notifications d'un membres

    public void supprimerNotificationMembre(Integer idUser,Integer idTontine){
        Membre membre = membreRepository.findByTontine_IdAndUser_Id(idTontine,idUser).orElseThrow(()->new RessourceNotFoundException("l'utilisateu "+idUser+" n'est pas membre de la tontine "+idTontine));
        notificationRepository.deleteByMembre_Id(membre.getId());
    }


    public NotificationResponse marquerLue(Integer idNotification,Boolean valeur) {
        Notification notification = notificationRepository.findById(idNotification).orElseThrow(() -> new RessourceNotFoundException("notification " + idNotification + " non trouvé"));
        notification.setEstLu(true);
        return notificationMapper.toNotificationResponse(notification);
    }

    // Marquer toutes les notifications d'un membre comme lues
    public void marquerToutesNotificationsCommeLues(Integer idUser, Integer idTontine) {
        Membre membre = membreRepository.findByTontine_IdAndUser_Id(idTontine, idUser)
                .orElseThrow(() -> new RessourceNotFoundException("L'utilisateur avec l'id " + idUser +
                        " n'est pas membre de la tontine avec l'id " + idTontine));

        //recuperer les notifications non lues de l'utilisateur dans la tontine
        List<Notification> notificationsNonLues = notificationRepository.findByMembre_IdAndEstLu(membre.getId(), false);

        for (Notification notification : notificationsNonLues) {
            notification.setEstLu(true);
            notification.setStatutNotification(StatutNotification.LU);
        }
        notificationRepository.saveAll(notificationsNonLues);

    }
}

