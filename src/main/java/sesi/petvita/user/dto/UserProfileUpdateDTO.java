package sesi.petvita.user.dto;

// DTO específico para o usuário atualizar o próprio perfil
public record UserProfileUpdateDTO(
        String username,
        String email,
        String phone,
        String address
) {}