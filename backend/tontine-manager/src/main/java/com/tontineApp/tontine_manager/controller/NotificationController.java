package com.tontineApp.tontine_manager.controller;

import com.tontineApp.tontine_manager.dto.NotificationRequest;
import com.tontineApp.tontine_manager.dto.NotificationResponse;
import com.tontineApp.tontine_manager.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/user/{userId}/tontine/{tontineId}")
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @PathVariable Integer userId,
            @PathVariable Integer tontineId
    ) {
        return ResponseEntity.ok(notificationService.getNotificationMembres(userId, tontineId));
    }

    @GetMapping("/user/{userId}/tontine/{tontineId}/non-lues")
    public ResponseEntity<List<NotificationResponse>> getNotificationsNonLues(
            @PathVariable Integer userId,
            @PathVariable Integer tontineId
    ) {
        return ResponseEntity.ok(notificationService.getNotificationNonLues(userId, tontineId));
    }

    @GetMapping("/user/{userId}/tontine/{tontineId}/non-lues/count")
    public ResponseEntity<Long> compterNotificationsNonLues(
            @PathVariable Integer userId,
            @PathVariable Integer tontineId
    ) {
        return ResponseEntity.ok(notificationService.compterNotificationsNonLues(userId, tontineId));
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(@RequestBody NotificationRequest notificationRequest) {
        NotificationResponse notificationResponse = notificationService.creerNotification(notificationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationResponse);
    }

    @PatchMapping("/{notificationId}/lire")
    public ResponseEntity<NotificationResponse> marqueCommeLue(@PathVariable Integer notificationId) {
        return ResponseEntity.ok(notificationService.marquerLue(notificationId, true));
    }

    @PatchMapping("/{notificationId}/non-lire")
    public ResponseEntity<NotificationResponse> marqueCommeNonLue(@PathVariable Integer notificationId) {
        return ResponseEntity.ok(notificationService.marquerLue(notificationId, false));
    }

    @PatchMapping("/user/{userId}/tontine/{tontineId}/lire-tout")
    public ResponseEntity<Void> marquerToutesNotificationsCommeLues(
            @PathVariable Integer userId,
            @PathVariable Integer tontineId
    ) {
        notificationService.marquerToutesNotificationsCommeLues(userId, tontineId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> supprimerNotification(@PathVariable Integer notificationId) {
        notificationService.supprimerNotification(notificationId);
        return ResponseEntity.noContent().build();
    }
}
