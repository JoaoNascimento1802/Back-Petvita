package sesi.petvita.clinic.dto;

import java.math.BigDecimal;

public record ClinicServiceDTO(
        Long id,
        String name,
        String description,
        BigDecimal price
) {}