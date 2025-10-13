package sesi.petvita.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import sesi.petvita.user.role.UserRole;

public record AdminUserCreateRequestDTO(
        @NotBlank @Size(min = 3, max = 50)
        String username,

        @NotBlank
        String password,

        @Email @NotBlank @Size(max = 100)
        String email,

        @NotBlank
        String phone,

        @NotBlank @Size(max = 200)
        String address,

        @NotBlank
        String rg,

        String imageurl,

        @NotNull
        UserRole role // Permite que o admin defina a role (USER, EMPLOYEE, etc.)
) {}
