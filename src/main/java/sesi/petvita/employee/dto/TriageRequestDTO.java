package sesi.petvita.employee.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TriageRequestDTO(
        @NotNull(message = "O peso é obrigatório.")
        Double weightKg,

        @NotNull(message = "A temperatura é obrigatória.")
        Double temperatureCelsius,

        @Size(min = 5, max = 500, message = "A queixa principal deve ter entre 5 e 500 caracteres.")
        String mainComplaint
) {}