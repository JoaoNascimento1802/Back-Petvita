// sesi/petvita/user/controller/UserController.java
package sesi.petvita.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sesi.petvita.user.dto.UserAuthResponseDTO; // Importar novo DTO
import sesi.petvita.user.dto.UserRequestDTO;
import sesi.petvita.user.dto.UserResponseDTO;
import sesi.petvita.user.dto.UserProfileUpdateDTO;
import sesi.petvita.user.mapper.UserMapper;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.user.service.UserService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Endpoints para registro e dados do usuário")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping("/register")
    @Operation(summary = "Registrar um novo usuário")
    public ResponseEntity<UserResponseDTO> registerUser(@Valid @RequestBody UserRequestDTO requestDTO) {
        UserResponseDTO registeredUser = userService.registerUser(requestDTO);
        return ResponseEntity.status(201).body(registeredUser);
    }

    @GetMapping("/me")
    @Operation(summary = "Verificar dados do usuário logado")
    public ResponseEntity<UserResponseDTO> getCurrentUser(@AuthenticationPrincipal UserModel user) {
        UserResponseDTO userResponse = userMapper.toDTO(user);
        return ResponseEntity.ok(userResponse);
    }

    /**
     * Atualiza o perfil do usuário logado.
     * Se o e-mail (que é usado para login) for alterado, um novo token JWT é retornado
     * para que o frontend possa atualizar o armazenamento local e manter o usuário logado.
     */
    @PutMapping("/me")
    @Operation(summary = "Atualizar dados do próprio perfil")
    public ResponseEntity<UserAuthResponseDTO> updateMyProfile(
            @AuthenticationPrincipal UserModel authenticatedUser,
            @RequestBody @Valid UserProfileUpdateDTO dto) {

        return ResponseEntity.ok(userService.updateUserProfile(authenticatedUser, dto));
    }
}