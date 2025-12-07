package sesi.petvita.veterinary.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public record WorkScheduleDTO(
        Long id,
        DayOfWeek dayOfWeek,
        LocalDate workDate, // Campo novo
        @JsonFormat(pattern = "HH:mm") LocalTime startTime,
        @JsonFormat(pattern = "HH:mm") LocalTime endTime,
        boolean isWorking
) {}
