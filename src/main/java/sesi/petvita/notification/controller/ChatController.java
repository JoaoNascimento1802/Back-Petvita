package sesi.petvita.notification.controller;

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
@Tag(name = "Chat", description = "Endpoints para o chat entre usuário e veterinário")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/{consultationId}")
    public ResponseEntity<Void> sendMessage(
            @PathVariable Long consultationId,
            @RequestBody String content,
            @AuthenticationPrincipal UserModel user) throws AccessDeniedException {

        chatService.sendMessage(consultationId, content, user);
        return ResponseEntity.ok().build();
    }
}