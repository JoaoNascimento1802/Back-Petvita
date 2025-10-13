package sesi.petvita.notification.dto;

import java.time.LocalDateTime;

public record ChatMessageResponseDTO(
        Long id,
        String content,
        LocalDateTime sentAt,
        SenderDTO sender // Usa o DTO simplificado, não o UserModel completo
) {}