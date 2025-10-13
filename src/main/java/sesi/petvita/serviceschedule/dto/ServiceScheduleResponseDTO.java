package sesi.petvita.serviceschedule.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record ServiceScheduleResponseDTO(
        Long id,
        LocalDate scheduleDate,
        LocalTime scheduleTime,
        String status,
        String observations,

        // Pet Info
        Long petId,
        String petName,

        // Client Info
        Long clientId,
        String clientName,

        // Employee Info
        Long employeeId,
        String employeeName,

        // Service Info
        Long serviceId,
        String serviceName,
        BigDecimal servicePrice
) {}
