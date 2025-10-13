package sesi.petvita.pet.dto;

import java.time.LocalDateTime;

public record MedicalRecordDTO(
        Long id,
        Long petId,
        Long consultationId,
        LocalDateTime recordDate,
        String anamnese,
        String diagnosis,
        String treatment
) {}