package sesi.petvita.consultation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record ConsultationRequestDTO(
        @NotNull @FutureOrPresent
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate consultationdate,

        @NotNull
        @JsonFormat(pattern = "HH:mm")
        LocalTime consultationtime,

        @NotNull
        Long clinicServiceId,

        @NotBlank @Size(min = 5, max = 255)
        String reason,

        String observations,

        @NotNull
        Long petId,

        @NotNull
        Long usuarioId,

        @NotNull
        Long veterinarioId
) {}