package sesi.petvita.veterinary.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record WorkScheduleDTO(
        Long id,
        Long veterinaryId,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        boolean isWorking
) {}