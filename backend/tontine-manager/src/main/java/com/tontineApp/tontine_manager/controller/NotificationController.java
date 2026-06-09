package com.tontineApp.tontine_manager.controller;

import com.tontineApp.tontine_manager.dto.NotificationResponse;
import com.tontineApp.tontine_manager.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    @GetMapping("user/{userId}/tontine/{tontineId}")
    public ResponseEntity<List<NotificationResponse>> getNotifications(@PathVariable Integer userId, @PathVariable Integer tontineId){
        List<NotificationResponse> notifications = notificationService.getNotificationMembres(userId,tontineId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("user/{userId}/tontine/{tontineId}/non-lues")
    public ResponseEntity<List<NotificationResponse>> getNotificationsNonLues(@PathVariable Integer userId, @PathVariable Integer tontineId){

    }


}
