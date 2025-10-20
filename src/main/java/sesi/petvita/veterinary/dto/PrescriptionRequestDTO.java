package sesi.petvita.veterinary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PrescriptionRequestDTO(
        @NotBlank(message = "O campo 'medicação' é obrigatório.")
        @Size(min = 3, max = 1000)
        String medication,

        @NotBlank(message = "O campo 'dosagem' é obrigatório.")
        @Size(min = 3, max = 1000)
        String dosage,

        @Size(max = 2000)
        String instructions
) {}