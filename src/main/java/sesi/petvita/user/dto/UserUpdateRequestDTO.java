package sesi.petvita.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserUpdateRequestDTO(
        @Size(min = 3, max = 50)
        String username,

        @Email @Size(max = 100)
        String email,

        @Pattern(regexp = "^[0-9]{8,15}$", message = "O telefone deve conter apenas números e ter entre 8 e 15 dígitos.")
        String phone,

        @Size(max = 200)
        String address,

        String imageurl,

        String password
) {}