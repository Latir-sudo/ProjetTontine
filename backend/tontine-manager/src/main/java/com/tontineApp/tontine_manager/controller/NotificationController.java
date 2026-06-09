package com.tontineApp.tontine_manager.controller;

import com.tontineApp.tontine_manager.dto.NotificationRequest;
import com.tontineApp.tontine_manager.dto.NotificationResponse;
import com.tontineApp.tontine_manager.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

        List<NotificationResponse> notifications = notificationService.getNotificationNonLues(userId,tontineId);
        return ResponseEntity.ok(notifications);
    }
    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(@RequestBody NotificationRequest notificationRequest){
        NotificationResponse notificationResponse = notificationService.creerNotification(notificationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationResponse);
    }
    @PatchMapping("/{notificationId}/lire")
    public ResponseEntity<NotificationResponse> marqueCommeLue(@PathVariable Integer notificationId){
        NotificationResponse notification = notificationService.marquerLue(notificationId,true);
        return ResponseEntity.ok(notification);

    }

    @PatchMapping("/{notificationId}/non-lire")
    public ResponseEntity<NotificationResponse> marqueCommeNonLue(@PathVariable Integer notificationId) {
        NotificationResponse notification = notificationService.marquerLue(notificationId, false);
        return ResponseEntity.ok(notification);
    }

    @PatchMapping("/{notificationId}/tous-lire")
    public ResponseEntity<List<NotificationResponse>> marqueToutesCommeLue(@PathVariable Integer notificationId) {
        List<NotificationResponse> notifications = notificationService.marquerToutesCommeLue(notificationId);
        return ResponseEntity.ok(notifications);
    }

}
