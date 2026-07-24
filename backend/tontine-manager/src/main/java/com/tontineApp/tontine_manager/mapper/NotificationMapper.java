package com.tontineApp.tontine_manager.mapper;

import com.tontineApp.tontine_manager.dto.NotificationRequest;
import com.tontineApp.tontine_manager.dto.NotificationResponse;
import com.tontineApp.tontine_manager.model.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public Notification toNotification(NotificationRequest notificationRequest) {
        Notification notification = new Notification();
        notification.setStatutNotification(notificationRequest.getStatutNotification());
        notification.setTypeNotification(notificationRequest.getTypeNotification());
        notification.setLienAction(notificationRequest.getLienAction());
        notification.setEstLu(Boolean.TRUE.equals(notificationRequest.getEstLu()));
        notification.setCouleur(notificationRequest.getCouleur());
        notification.setHeureRelative(notificationRequest.getTempsRelatif());
        notification.setMessage(notificationRequest.getMessage());
        notification.setTitre(notificationRequest.getTitre());

        return notification;
    }

    public NotificationResponse toNotificationResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setTitre(notification.getTitre());
        response.setMessage(notification.getMessage());
        response.setTempsRelatif(notification.getTempsRelatif());
        response.setCouleur(notification.getCouleur());
        response.setEstLu(notification.getEstLu());
        response.setDateCreation(notification.getDateCreation());
        response.setStatutNotification(notification.getStatutNotification());
        response.setLienAction(notification.getLienAction());
        response.setTypeNotification(notification.getTypeNotification());

        if (notification.getMembre() != null) {
            if (notification.getMembre().getUser() != null) {
                response.setIdUser(notification.getMembre().getUser().getId());
                response.setNomUser(notification.getMembre().getUser().getNom());
                response.setPrenomUser(notification.getMembre().getUser().getPrenom());
            }

            if (notification.getMembre().getTontine() != null) {
                response.setIdTontine(notification.getMembre().getTontine().getId());
                response.setNomTontine(notification.getMembre().getTontine().getNomTontine());
            }
        } else if (notification.getUser() != null) {
            response.setIdUser(notification.getUser().getId());
            response.setNomUser(notification.getUser().getNom());
            response.setPrenomUser(notification.getUser().getPrenom());
        }

        return response;
    }
}
