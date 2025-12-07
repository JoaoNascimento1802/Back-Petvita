package sesi.petvita.serviceschedule.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ServiceScheduleUpdateRequestDTO(
        LocalDate scheduleDate,
        LocalTime scheduleTime,
        String observations
) {}
