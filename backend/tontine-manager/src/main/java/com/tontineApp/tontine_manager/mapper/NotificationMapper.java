package com.tontineApp.tontine_manager.mapper;


import com.tontineApp.tontine_manager.dto.NotificationRequest;
import com.tontineApp.tontine_manager.dto.NotificationResponse;
import com.tontineApp.tontine_manager.model.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public  Notification toNotification(NotificationRequest notificationRequest) {
        Notification notification = new Notification();
        notification.setStatutNotification(notificationRequest.getStatutNotification());
        notification.setTypeNotification(notificationRequest.getTypeNotification());
        notification.setLienAction(notificationRequest.getLienAction());
        notification.setEstLu(notificationRequest.getEstLu());
        notification.setDateCreation(notification.getDateCreation());
        notification.setCouleur(notification.getCouleur());
        notification.setHeureRelative(notification.getHeureRelative());
        notification.setMessage(notificationRequest.getMessage());
        notification.setTitre(notificationRequest.getTitre());

        // la mise a jour de tontine et membre se fera dans la couche service
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
            response.setIdMembre(notification.getMembre().getId());
            response.setNomMembre(notification.getMembre().getUser().getNom());
            response.setPrenomMembre(notification.getMembre().getUser().getPrenom());
        }

        if (notification.getTontine() != null) {
            response.setIdTontine(notification.getTontine().getId());
            response.setNomTontine(notification.getTontine().getNomTontine());
        }

        return response;
    }
}
