// sesi/petvita/notification/service/ChatService.java
package sesi.petvita.notification.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sesi.petvita.consultation.model.ConsultationModel;
import sesi.petvita.consultation.repository.ConsultationRepository;
// --- NOVOS IMPORTS ---
import sesi.petvita.serviceschedule.model.ServiceScheduleModel;
import sesi.petvita.serviceschedule.repository.ServiceScheduleRepository;
// --- FIM DOS NOVOS IMPORTS ---
import sesi.petvita.user.model.UserModel;
import sesi.petvita.user.role.UserRole;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConsultationRepository consultationRepository;
    private final ServiceScheduleRepository serviceScheduleRepository; // --- ADICIONADO ---
    private final NotificationService notificationService;
    private final Firestore firestore;

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    @Transactional
    public void sendMessageToConsultation(Long consultationId, String content, UserModel sender) throws AccessDeniedException {

        log.info("Iniciando sendMessage (Consulta) para consultaId: {}", consultationId);
        ConsultationModel consultation = findConsultationById(consultationId);

        if (!isUserAuthorizedForChat(consultation, sender)) {
            log.warn("Falha de autorização: Usuário {} não autorizado para chat da consulta {}", sender.getId(), consultationId);
            throw new AccessDeniedException("Você não tem permissão para enviar mensagens neste chat.");
        }

        UserModel receiver;
        UserModel consultationUser = consultation.getUsuario();
        UserModel vetUserAccount = consultation.getVeterinario().getUserAccount();

        if (vetUserAccount == null) {
            log.error("Falha crítica: Veterinário (ID: {}) não possui conta de usuário associada.", consultation.getVeterinario().getId());
            throw new IllegalStateException("O veterinário desta consulta não tem uma conta de usuário associada.");
        }

        if (sender.getId().equals(consultationUser.getId())) {
            receiver = vetUserAccount;
        } else {
            receiver = consultationUser;
        }
        log.info("Mensagem (Consulta) de {} para {}", sender.getActualUsername(), receiver.getActualUsername());

        Map<String, Object> messageData = createMessageData(sender, content);

        try {
            log.info("Enviando dados para o Firestore (consultas/{}/mensagens)...", consultationId);

            DocumentReference messageRef = firestore.collection("consultas")
                    .document(consultationId.toString())
                    .collection("mensagens")
                    .document();

            ApiFuture<WriteResult> future = messageRef.set(messageData);
            WriteResult result = future.get();
            log.info("Mensagem (Consulta) salva no Firestore! Update time: {}", result.getUpdateTime());

        } catch (InterruptedException | ExecutionException e) {
            log.error("!!! ERRO AO ESCREVER (Consulta) NO FIREBASE !!!: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao salvar a mensagem no Firebase.", e);
        } catch (Exception e) {
            log.error("Erro inesperado no Firestore (Consulta): {}", e.getMessage(), e);
            throw new RuntimeException("Erro inesperado no chat.", e);
        }

        String notificationMessage = "Você tem uma nova mensagem de " + sender.getActualUsername() + ".";

        if (sender.getRole() == UserRole.USER) {
            notificationService.createNotification(vetUserAccount, notificationMessage, consultationId);
        } else if (sender.getRole() == UserRole.VETERINARY) {
            notificationService.createNotification(consultationUser, notificationMessage, consultationId);
        } else if (sender.getRole() == UserRole.EMPLOYEE || sender.getRole() == UserRole.ADMIN) {
            notificationService.createNotification(consultationUser, notificationMessage, consultationId);
            notificationService.createNotification(vetUserAccount, notificationMessage, consultationId);
        }
    }

    // --- NOVO MÉTODO ADICIONADO ---
    @Transactional
    public void sendMessageToService(Long serviceScheduleId, String content, UserModel sender) throws AccessDeniedException {

        log.info("Iniciando sendMessage (Serviço) para serviceScheduleId: {}", serviceScheduleId);
        ServiceScheduleModel service = findServiceScheduleById(serviceScheduleId);

        if (!isUserAuthorizedForServiceChat(service, sender)) {
            log.warn("Falha de autorização: Usuário {} não autorizado para chat do serviço {}", sender.getId(), serviceScheduleId);
            throw new AccessDeniedException("Você não tem permissão para enviar mensagens neste chat.");
        }

        UserModel receiver;
        UserModel clientUser = service.getClient();
        UserModel employeeUser = service.getEmployee();

        if (sender.getId().equals(clientUser.getId())) {
            receiver = employeeUser;
        } else {
            receiver = clientUser;
        }
        log.info("Mensagem (Serviço) de {} para {}", sender.getActualUsername(), receiver.getActualUsername());

        Map<String, Object> messageData = createMessageData(sender, content);

        try {
            log.info("Enviando dados para o Firestore (services/{}/mensagens)...", serviceScheduleId);

            // Salva em uma *nova* coleção "services"
            DocumentReference messageRef = firestore.collection("services")
                    .document(serviceScheduleId.toString())
                    .collection("mensagens")
                    .document();

            ApiFuture<WriteResult> future = messageRef.set(messageData);
            WriteResult result = future.get();
            log.info("Mensagem (Serviço) salva no Firestore! Update time: {}", result.getUpdateTime());

        } catch (InterruptedException | ExecutionException e) {
            log.error("!!! ERRO AO ESCREVER (Serviço) NO FIREBASE !!!: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao salvar a mensagem no Firebase.", e);
        } catch (Exception e) {
            log.error("Erro inesperado no Firestore (Serviço): {}", e.getMessage(), e);
            throw new RuntimeException("Erro inesperado no chat.", e);
        }

        // Envia notificação SQL
        String notificationMessage = "Você tem uma nova mensagem de " + sender.getActualUsername() + ".";
        notificationService.createNotification(receiver, notificationMessage, null); // null_ID_de_consulta
    }

    private Map<String, Object> createMessageData(UserModel sender, String content) {
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("senderId", sender.getId());
        messageData.put("senderName", sender.getActualUsername());
        messageData.put("content", content);
        messageData.put("timestamp", com.google.cloud.Timestamp.now());
        return messageData;
    }

    // --- MÉTODOS DE BUSCA E AUTORIZAÇÃO SEPARADOS ---

    private ConsultationModel findConsultationById(Long consultationId) {
        return consultationRepository.findByIdWithDetails(consultationId)
                .orElseThrow(() -> new NoSuchElementException("Consulta não encontrada com o ID: " + consultationId));
    }

    private ServiceScheduleModel findServiceScheduleById(Long serviceScheduleId) {
        // Você precisará criar este método findByIdWithDetails no ServiceScheduleRepository
        return serviceScheduleRepository.findByIdWithDetails(serviceScheduleId)
                .orElseThrow(() -> new NoSuchElementException("Agendamento de serviço não encontrado com o ID: " + serviceScheduleId));
    }

    private boolean isUserAuthorizedForChat(ConsultationModel consultation, UserModel user) {
        if (user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.EMPLOYEE) {
            return true;
        }
        Long consultationUserId = consultation.getUsuario().getId();
        Long vetUserAccountId = (consultation.getVeterinario().getUserAccount() != null)
                ? consultation.getVeterinario().getUserAccount().getId() : -1L;

        return user.getId().equals(consultationUserId) || user.getId().equals(vetUserAccountId);
    }

    private boolean isUserAuthorizedForServiceChat(ServiceScheduleModel service, UserModel user) {
        if (user.getRole() == UserRole.ADMIN) {
            return true;
        }
        Long clientUserId = service.getClient().getId();
        Long employeeUserId = service.getEmployee().getId();

        return user.getId().equals(clientUserId) || user.getId().equals(employeeUserId);
    }
}