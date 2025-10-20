package sesi.petvita.employee.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

// DTO para o funcionário agendar uma consulta para um cliente
public record EmployeeConsultationRequestDTO(
        @NotNull Long petId,
        @NotNull Long usuarioId, // ID do cliente (tutor)
        @NotNull Long veterinarioId,
        @NotNull Long clinicServiceId,

        @NotNull @FutureOrPresent
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate consultationdate,

        @NotNull
        @JsonFormat(pattern = "HH:mm")
        LocalTime consultationtime,

        @NotBlank @Size(min = 5, max = 255)
        String reason,

        String observations
) {}