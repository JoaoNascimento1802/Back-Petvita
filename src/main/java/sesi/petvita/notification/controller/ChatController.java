// sesi/petvita/notification/controller/ChatController.java
package sesi.petvita.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sesi.petvita.notification.service.ChatService;
import sesi.petvita.user.model.UserModel;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Endpoints para o chat (Firebase)")
public class ChatController {

    private final ChatService chatService;

    // ROTA MODIFICADA: Agora específica para Consultas (Vets)
    @PostMapping("/consultation/{consultationId}")
    @Operation(summary = "Enviar mensagem em um chat de CONSULTA (Veterinário)")
    public ResponseEntity<Void> sendMessageToConsultation(
            @PathVariable Long consultationId,
            @RequestBody String content,
            @AuthenticationPrincipal UserModel user) throws AccessDeniedException {

        chatService.sendMessageToConsultation(consultationId, content, user);
        return ResponseEntity.ok().build();
    }

    // --- NOVA ROTA ADICIONADA ---
    // Rota específica para Serviços (Funcionários)
    @PostMapping("/service/{serviceScheduleId}")
    @Operation(summary = "Enviar mensagem em um chat de SERVIÇO (Funcionário)")
    public ResponseEntity<Void> sendMessageToService(
            @PathVariable Long serviceScheduleId,
            @RequestBody String content,
            @AuthenticationPrincipal UserModel user) throws AccessDeniedException {

        chatService.sendMessageToService(serviceScheduleId, content, user);
        return ResponseEntity.ok().build();
    }
}