package sesi.petvita.vaccine.dto;

import java.time.LocalDate;

public record VaccineResponseDTO(
        Long id,
        String name,
        String manufacturer,
        String batch,
        LocalDate applicationDate,
        LocalDate nextDoseDate,
        String veterinaryName, // Nome do vet que aplicou
        String petName,
        String observations
) {}