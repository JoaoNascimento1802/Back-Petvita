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
        String employeeReport, // --- NOVO CAMPO ADICIONADO ---
        Long petId,
        String petName,
        Long clientId,
        String clientName,
        Long employeeId,
        String employeeName,
        Long serviceId,
        String serviceName,
        BigDecimal servicePrice
) {}