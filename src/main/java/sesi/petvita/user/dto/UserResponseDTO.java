package sesi.petvita.user.dto;

import sesi.petvita.user.role.UserRole;

// CORREÇÃO: Adicionados os campos 'address' e 'imageurl'
public record UserResponseDTO(
        Long id,
        String username,
        String email,
        String phone,
        String address,    // <-- ADICIONADO
        String imageurl,   // <-- ADICIONADO
        UserRole role
) {}