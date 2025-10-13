package sesi.petvita.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sesi.petvita.user.dto.UserRequestDTO;
import sesi.petvita.user.dto.UserResponseDTO;
import sesi.petvita.user.dto.UserProfileUpdateDTO; // Assegure-se de que este é o import correto
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

    // CORREÇÃO: Garante que existe apenas UM método para PUT /users/me
    @PutMapping("/me")
    @Operation(summary = "Atualizar dados do próprio perfil")
    public ResponseEntity<UserResponseDTO> updateMyProfile(
            @AuthenticationPrincipal UserModel authenticatedUser,
            @RequestBody @Valid UserProfileUpdateDTO dto) { // Usa o DTO específico de perfil

        return ResponseEntity.ok(userService.updateUserProfile(authenticatedUser.getId(), dto));
    }
}