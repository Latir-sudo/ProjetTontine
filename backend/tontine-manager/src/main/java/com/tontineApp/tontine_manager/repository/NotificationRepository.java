package com.tontineApp.tontine_manager.repository;

import com.tontineApp.tontine_manager.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification,Integer> {
}
