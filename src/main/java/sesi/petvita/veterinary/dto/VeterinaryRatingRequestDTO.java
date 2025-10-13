package sesi.petvita.veterinary.dto;

import jakarta.validation.constraints.*;

public record VeterinaryRatingRequestDTO(
        @NotNull
        @Min(1)
        @Max(5)
        Double rating,
        String comment
) {}