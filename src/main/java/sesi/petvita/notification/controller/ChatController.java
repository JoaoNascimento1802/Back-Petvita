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
import java.util.Map; // Import necessário

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Endpoints para o chat (Firebase)")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/consultation/{consultationId}")
    @Operation(summary = "Enviar mensagem em um chat de CONSULTA")
    public ResponseEntity<Void> sendMessageToConsultation(
            @PathVariable Long consultationId,
            @RequestBody Map<String, String> payload, // CORREÇÃO: Recebe um Objeto JSON
            @AuthenticationPrincipal UserModel user) throws AccessDeniedException {

        // Extrai apenas o texto da mensagem
        String content = payload.get("content");

        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        chatService.sendMessageToConsultation(consultationId, content, user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/service/{serviceScheduleId}")
    @Operation(summary = "Enviar mensagem em um chat de SERVIÇO")
    public ResponseEntity<Void> sendMessageToService(
            @PathVariable Long serviceScheduleId,
            @RequestBody Map<String, String> payload, // CORREÇÃO: Recebe um Objeto JSON
            @AuthenticationPrincipal UserModel user) throws AccessDeniedException {

        // Extrai apenas o texto da mensagem
        String content = payload.get("content");

        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        chatService.sendMessageToService(serviceScheduleId, content, user);
        return ResponseEntity.ok().build();
    }
}