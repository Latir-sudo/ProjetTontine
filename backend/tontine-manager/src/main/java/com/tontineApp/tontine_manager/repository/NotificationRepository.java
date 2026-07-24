package com.tontineApp.tontine_manager.repository;

import com.tontineApp.tontine_manager.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification,Integer> {


    List<Notification> findByMembre_Id(Integer id);
    void deleteByMembre_Id(Integer id);
    List<Notification> findByMembre_IdAndEstLu(Integer id, Boolean estLue);

    // Toutes les notifications d'un utilisateur, toutes tontines confondues
    List<Notification> findByMembre_User_IdOrderByDateCreationDesc(Integer userId);

    // Notifications liées directement à un utilisateur (sans passer par Membre)
    List<Notification> findByUser_IdOrderByDateCreationDesc(Integer userId);
}
