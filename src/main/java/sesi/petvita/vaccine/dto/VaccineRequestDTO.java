package sesi.petvita.vaccine.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record VaccineRequestDTO(
        @NotBlank String name,
        String manufacturer,
        String batch,
        @NotNull LocalDate applicationDate,
        LocalDate nextDoseDate, // Opcional
        @NotNull Long petId,
        String observations
) {}