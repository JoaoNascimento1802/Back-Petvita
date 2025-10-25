package sesi.petvita.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRequestDTO(
        @NotBlank @Size(min = 3, max = 50)
        String username,

        @NotBlank
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).+$", message = "A senha deve ser forte.")
        String password,

        @Email @NotBlank @Size(max = 100)
        String email,

        // --- CORREÇÃO APLICADA AQUI ---
        // A regra agora permite apenas números, com 8 a 15 dígitos, sem formato fixo.
        @NotBlank @Pattern(regexp = "^[0-9]{8,15}$", message = "O telefone deve conter apenas números e ter entre 8 e 15 dígitos.")
        String phone,

        @NotBlank @Size(max = 200)
        String address,

        @NotBlank @Pattern(regexp = "^\\d{7,9}X?$", message = "Formato de RG inválido")
        String rg,

        String imageurl
) {}