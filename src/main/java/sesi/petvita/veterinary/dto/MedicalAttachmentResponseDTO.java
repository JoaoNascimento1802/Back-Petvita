package sesi.petvita.veterinary.dto;

import java.time.LocalDateTime;

public record MedicalAttachmentResponseDTO(
        Long id,
        Long medicalRecordId,
        String fileName,
        String fileUrl,
        LocalDateTime createdAt
) {}