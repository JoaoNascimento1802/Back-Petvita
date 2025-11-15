// sesi/petvita/user/dto/UserAuthResponseDTO.java
package sesi.petvita.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import sesi.petvita.user.dto.UserResponseDTO;

/**
 * DTO de resposta para autenticação ou atualização de perfil.
 * Inclui os dados do usuário e, opcionalmente, um novo token JWT.
 * O token só será enviado se o e-mail (login) do usuário for alterado.
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // Não inclui o campo 'token' se ele for nulo
public record UserAuthResponseDTO(
        UserResponseDTO user,
        String token
) {}