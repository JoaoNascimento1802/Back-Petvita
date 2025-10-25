package sesi.petvita.veterinary.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import sesi.petvita.veterinary.speciality.SpecialityEnum;

public record VeterinaryRequestDTO(
        @NotBlank @Size(min = 3, max = 50)
        String name,

        String password,

        @Email @NotBlank @Size(max = 100)
        String email,

        @NotBlank @Size(min = 6, max = 15)
        @Pattern(regexp = "^[A-Za-z]{2}\\s?\\d+$", message = "Formato de CRMV inválido")
        String crmv,

        @NotBlank @Pattern(regexp = "^\\d{7,9}X?$", message = "Formato de RG inválido")
        String rg,

        SpecialityEnum specialityenum,

        // --- CORREÇÃO APLICADA AQUI ---
        // A regra agora permite apenas números, com 8 a 15 dígitos, sem formato fixo.
        @NotBlank @Pattern(regexp = "^[0-9]{8,15}$", message = "O telefone deve conter apenas números e ter entre 8 e 15 dígitos.")
        String phone,

        @NotBlank
        String imageurl
) {}