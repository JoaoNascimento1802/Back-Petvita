package sesi.petvita.consultation.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ConsultationUpdateRequestDTO(
        LocalDate consultationdate,
        LocalTime consultationtime,
        String reason,
        String observations
) {}