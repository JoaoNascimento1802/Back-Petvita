package sesi.petvita.clinic.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import sesi.petvita.veterinary.speciality.SpecialityEnum;

import java.math.BigDecimal;

public record ClinicServiceRequestDTO(
        @NotBlank(message = "O nome do serviço é obrigatório.")
        @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres.")
        String name,

        String description,

        @NotNull(message = "O preço é obrigatório.")
        @DecimalMin(value = "0.0", inclusive = false, message = "O preço deve ser maior que zero.")
        BigDecimal price,

        @NotNull(message = "A especialidade é obrigatória.")
        SpecialityEnum speciality,

        @NotNull(message = "É obrigatório definir se o serviço é médico.")
        Boolean isMedicalService
) {}