package com.tontineApp.tontine_manager.service;

import com.tontineApp.tontine_manager.dto.NotificationRequest;
import com.tontineApp.tontine_manager.dto.NotificationResponse;
import com.tontineApp.tontine_manager.exception.RessourceNotFoundException;
import com.tontineApp.tontine_manager.mapper.NotificationMapper;
import com.tontineApp.tontine_manager.model.Membre;
import com.tontineApp.tontine_manager.repository.MembreRepository;
import com.tontineApp.tontine_manager.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final MembreRepository membreRepository;

    public NotificationResponse creerNotification(NotificationRequest notificationRequest) {
        return notificationMapper.toNotificationResponse(notificationRepository.save(notificationMapper.toNotification(notificationRequest)));
    }

    public void supprimerNotification(Integer id) {
        notificationRepository.deleteById(id);
    }

    public List<NotificationResponse> getNotifcationMembres(Integer idUser,Integer idTontine) {
        Membre membre = membreRepository.findByTontine_IdAndUser_Id(idTontine,idUser).orElseThrow(()->new RessourceNotFoundException("l'utilisateu "+idUser+" n'est pas membre de la tontine "+idTontine));
        return notificationMapper.toNotificationResponse(notificationRepository.findAllById(membre.getId()));
    }
}
