package sesi.petvita.veterinary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PrescriptionTemplateRequestDTO(
        @NotBlank @Size(min = 3, max = 100)
        String title,

        @NotBlank @Size(min = 3, max = 1000)
        String medication,

        @NotBlank @Size(min = 3, max = 1000)
        String dosage,

        @Size(max = 2000)
        String instructions
) {}