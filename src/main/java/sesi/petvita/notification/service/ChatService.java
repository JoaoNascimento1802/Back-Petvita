package sesi.petvita.notification.service;

import com.google.cloud.firestore.Firestore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sesi.petvita.consultation.model.ConsultationModel;
import sesi.petvita.consultation.repository.ConsultationRepository;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.user.role.UserRole;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConsultationRepository consultationRepository;
    private final NotificationService notificationService;
    private final Firestore firestore;

    public void sendMessage(Long consultationId, String content, UserModel sender) throws AccessDeniedException {
        ConsultationModel consultation = findConsultationById(consultationId);

        if (!isUserAuthorizedForChat(consultation, sender)) {
            throw new AccessDeniedException("Você não tem permissão para enviar mensagens neste chat.");
        }

        UserModel receiver;
        UserModel consultationUser = consultation.getUsuario();
        UserModel vetUserAccount = consultation.getVeterinario().getUserAccount();

        if (vetUserAccount == null) {
            throw new IllegalStateException("O veterinário desta consulta não tem uma conta de usuário associada.");
        }

        if (sender.getId().equals(consultationUser.getId())) {
            receiver = vetUserAccount;
        } else {
            receiver = consultationUser;
        }

        Map<String, Object> messageData = new HashMap<>();
        messageData.put("senderId", sender.getId());
        messageData.put("senderName", sender.getActualUsername()); // Corrigido para usar o nome real
        messageData.put("content", content);
        messageData.put("timestamp", com.google.cloud.Timestamp.now());

        firestore.collection("consultas")
                .document(consultationId.toString())
                .collection("mensagens")
                .add(messageData);

        // ===== CORREÇÃO AQUI: Adicionado o consultationId como terceiro argumento =====
        notificationService.createNotification(receiver, "Você tem uma nova mensagem de " + sender.getActualUsername() + ".", consultationId);
    }

    private ConsultationModel findConsultationById(Long consultationId) {
        return consultationRepository.findById(consultationId)
                .orElseThrow(() -> new NoSuchElementException("Consulta não encontrada com o ID: " + consultationId));
    }

    private boolean isUserAuthorizedForChat(ConsultationModel consultation, UserModel user) {
        if (user.getRole() == UserRole.ADMIN) {
            return true;
        }

        Long consultationUserId = consultation.getUsuario().getId();
        Long vetUserAccountId = (consultation.getVeterinario().getUserAccount() != null)
                ? consultation.getVeterinario().getUserAccount().getId()
                : -1L;

        return user.getId().equals(consultationUserId) || user.getId().equals(vetUserAccountId);
    }
}