// sesi/petvita/user/dto/UserResponseDTO.java
package sesi.petvita.user.dto;

import sesi.petvita.user.role.UserRole;

// Ordem estrita: ID, Username, Email, Phone, Address, ImageUrl, Role
public record UserResponseDTO(
        Long id,
        String username,
        String email,
        String phone,
        String address,
        String imageurl,
        UserRole role
) {}