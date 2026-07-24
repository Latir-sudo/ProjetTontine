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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.tontineApp.tontine_manager.model.Users;

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

    /**
     * Récupère toutes les notifications d'un utilisateur, toutes tontines confondues.
     * Inclut les notifications liées via Membre ET celles liées directement via User.
     * Exposé via GET /api/notifications/user/{userId}
     */
    public List<NotificationResponse> getAllNotificationsUser(Integer idUser) {
        List<Notification> membreNotifications = notificationRepository.findByMembre_User_IdOrderByDateCreationDesc(idUser);
        List<Notification> userNotifications = notificationRepository.findByUser_IdOrderByDateCreationDesc(idUser);

        // Merge and deduplicate
        Map<Integer, Notification> merged = new LinkedHashMap<>();
        for (Notification n : membreNotifications) merged.put(n.getId(), n);
        for (Notification n : userNotifications) merged.put(n.getId(), n);

        return merged.values().stream()
                .sorted(Comparator.comparing(Notification::getDateCreation).reversed())
                .map(notificationMapper::toNotificationResponse)
                .toList();
    }

    /**
     * Crée une notification directement liée à un utilisateur (pas un membre).
     * Utilisé pour notifier un utilisateur qui n'est pas encore membre d'une tontine.
     */
    public void creerNotificationUtilisateur(Users user, String titre, String message,
                                             String typeNotification, String couleur, String lienAction) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitre(titre);
        notification.setMessage(message);
        notification.setTypeNotification(typeNotification);
        notification.setCouleur(couleur != null ? couleur : "#0052cc");
        notification.setLienAction(lienAction);
        notification.setEstLu(false);
        notification.setStatutNotification(StatutNotification.NON_LU);
        notification.setDateCreation(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    /**
     * Crée une notification directement à partir d'un objet Membre déjà résolu.
     * Utilisé par AdhesionService pour éviter une seconde requête BDD.
     */
    public void creerNotificationDirecte(Membre membre, String titre, String message,
                                         String typeNotification, String couleur, String lienAction) {
        Notification notification = new Notification();
        notification.setMembre(membre);
        notification.setTitre(titre);
        notification.setMessage(message);
        notification.setTypeNotification(typeNotification);
        notification.setCouleur(couleur != null ? couleur : "#0052cc");
        notification.setLienAction(lienAction);
        notification.setEstLu(false);
        notification.setStatutNotification(StatutNotification.NON_LU);
        notification.setDateCreation(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    private Membre getMembre(Integer idUser, Integer idTontine) {
        return membreRepository.findByTontine_IdAndUser_Id(idTontine, idUser)
                .orElseThrow(() -> new RessourceNotFoundException(
                        "L'utilisateur avec l'id " + idUser + " n'est pas membre de la tontine avec l'id " + idTontine
                ));
    }
}
