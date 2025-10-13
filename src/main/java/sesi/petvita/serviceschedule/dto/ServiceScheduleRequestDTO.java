package sesi.petvita.serviceschedule.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record ServiceScheduleRequestDTO(
        @NotNull
        Long petId,

        @NotNull
        Long employeeId,

        @NotNull
        Long clinicServiceId,

        @NotNull
        @FutureOrPresent
        LocalDate scheduleDate,

        @NotNull
        LocalTime scheduleTime,

        String observations
) {}
