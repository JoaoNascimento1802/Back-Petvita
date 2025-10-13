package sesi.petvita.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sesi.petvita.notification.model.Notification;
import sesi.petvita.notification.repository.NotificationRepository;
import sesi.petvita.user.model.UserModel;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    @Transactional
    public void createNotification(UserModel user, String message, Long consultationId) {
        Notification notification = Notification.builder()
                .user(user)
                .message(message)
                .isRead(false)
                .consultationId(consultationId)
                .build();
        notificationRepository.save(notification);
    }

    public List<Notification> getNotificationsForUser(UserModel user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional
    public void markAsRead(Long notificationId, UserModel user) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NoSuchElementException("Notificação não encontrada"));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("Você não tem permissão para marcar esta notificação como lida.");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }
}