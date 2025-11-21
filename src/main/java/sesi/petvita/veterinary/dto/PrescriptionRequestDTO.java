package sesi.petvita.veterinary.dto;

// Removemos as validações para permitir texto livre ou campos vazios
public record PrescriptionRequestDTO(
        String medicationName,
        String dosage,
        String frequency,
        String duration
) {}