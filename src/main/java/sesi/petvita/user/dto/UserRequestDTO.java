// sesi/petvita/user/dto/UserRequestDTO.java
package sesi.petvita.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import sesi.petvita.user.role.UserRole;

public record UserRequestDTO(
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

        String crmv,

        // Este campo precisa existir para o Mapper ler
        UserRole role
) {}