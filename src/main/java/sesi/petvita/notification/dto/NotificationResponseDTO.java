// sesi/petvita/notification/dto/NotificationResponseDTO.java
package sesi.petvita.notification.dto;

import lombok.Getter;
import lombok.Setter;
import sesi.petvita.user.dto.UserResponseDTO; // Importa o DTO de usuário
import java.time.LocalDateTime;

@Getter
@Setter
public class NotificationResponseDTO {
    private Long id;
    private UserResponseDTO user; // Usa o DTO de usuário, não a entidade
    private String message;
    private boolean isRead;
    private LocalDateTime createdAt;
    private Long consultationId;

    public NotificationResponseDTO(Long id, UserResponseDTO user, String message, boolean isRead, LocalDateTime createdAt, Long consultationId) {
        this.id = id;
        this.user = user;
        this.message = message;
        this.isRead = isRead;
        this.createdAt = createdAt;
        this.consultationId = consultationId;
    }
}