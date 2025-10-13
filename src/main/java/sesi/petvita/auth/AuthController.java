package sesi.petvita.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.user.service.UserService;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        var user = (UserModel) authentication.getPrincipal();
        var jwtToken = tokenService.generateToken(user);
        return ResponseEntity.ok(new AuthResponseDTO(jwtToken));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        userService.requestPasswordReset(email);
        return ResponseEntity.ok(Map.of("message", "Se o e-mail estiver cadastrado, um link de redefinição será enviado."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestParam("token") String token, @RequestBody Map<String, String> request) {
        String newPassword = request.get("password");
        userService.resetPassword(token, newPassword);
        return ResponseEntity.ok(Map.of("message", "Senha redefinida com sucesso."));
    }
}