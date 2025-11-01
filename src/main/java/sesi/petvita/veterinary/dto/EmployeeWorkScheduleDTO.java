package sesi.petvita.veterinary.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.DayOfWeek;
import java.time.LocalTime;

public record EmployeeWorkScheduleDTO(
        Long id,
        Long employeeId, // Específico para Funcionário
        DayOfWeek dayOfWeek,
        @JsonFormat(pattern = "HH:mm") LocalTime startTime,
        @JsonFormat(pattern = "HH:mm") LocalTime endTime,
        boolean isWorking
) {}