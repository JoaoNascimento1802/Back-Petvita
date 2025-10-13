package sesi.petvita.notification.mapper;

import org.springframework.stereotype.Component;
import sesi.petvita.notification.dto.ChatMessageResponseDTO;
import sesi.petvita.notification.dto.SenderDTO;
import sesi.petvita.notification.model.ChatMessage;
import sesi.petvita.user.model.UserModel;

@Component
public class ChatMessageMapper {

    public ChatMessageResponseDTO toDTO(ChatMessage message) {
        if (message == null) {
            return null;
        }

        UserModel senderModel = message.getSender();
        SenderDTO senderDTO = new SenderDTO(senderModel.getId(), senderModel.getUsername());

        return new ChatMessageResponseDTO(
                message.getId(),
                message.getContent(),
                message.getSentAt(),
                senderDTO
        );
    }
}