package sesi.petvita.veterinary.dto;

public record PrescriptionTemplateResponseDTO(
        Long id,
        String title,
        String medication,
        String dosage,
        String instructions
) {}