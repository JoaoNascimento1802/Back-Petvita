package sesi.petvita.veterinary.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.DayOfWeek;
import java.time.LocalTime;

public record VeterinaryWorkScheduleDTO(
        Long id,
        Long veterinaryId, // Específico para Veterinário
        DayOfWeek dayOfWeek,
        @JsonFormat(pattern = "HH:mm") LocalTime startTime,
        @JsonFormat(pattern = "HH:mm") LocalTime endTime,
        boolean isWorking
) {}