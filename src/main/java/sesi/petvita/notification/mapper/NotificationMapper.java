// sesi/petvita/notification/mapper/NotificationMapper.java
package sesi.petvita.notification.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import sesi.petvita.notification.dto.NotificationResponseDTO;
import sesi.petvita.notification.model.Notification;
import sesi.petvita.user.mapper.UserMapper;

@Component
@RequiredArgsConstructor
public class NotificationMapper {

    private final UserMapper userMapper; // Reutiliza o mapper de usuário

    public NotificationResponseDTO toDTO(Notification model) {
        return new NotificationResponseDTO(
                model.getId(),
                userMapper.toDTO(model.getUser()), // Converte o UserModel para UserResponseDTO
                model.getMessage(),
                model.isRead(),
                model.getCreatedAt(),
                model.getConsultationId()
        );
    }
}