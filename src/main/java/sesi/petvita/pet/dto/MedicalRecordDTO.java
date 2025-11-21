package sesi.petvita.pet.dto;

import java.time.LocalDateTime;
import java.util.List;

public record MedicalRecordDTO(
        Long id,
        LocalDateTime createdAt,
        String diagnosis,
        String treatment,
        String veterinaryName,
        Long consultationId,
        List<AttachmentDTO> attachments,
        List<PrescriptionDTO> prescriptions
) {}

record AttachmentDTO(Long id, String fileName, String fileUrl, String fileType) {}
record PrescriptionDTO(Long id, String medicationName, String dosage, String frequency, String duration, String observations) {}