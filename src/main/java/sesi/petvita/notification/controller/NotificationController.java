package sesi.petvita.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sesi.petvita.notification.model.Notification;
import sesi.petvita.notification.service.NotificationService;
import sesi.petvita.user.model.UserModel;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<Notification>> getMyNotifications(@AuthenticationPrincipal UserModel user) {
        return ResponseEntity.ok(notificationService.getNotificationsForUser(user));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, @AuthenticationPrincipal UserModel user) {
        notificationService.markAsRead(id, user);
        return ResponseEntity.ok().build();
    }
}