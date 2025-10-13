package sesi.petvita.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import sesi.petvita.consultation.model.ConsultationModel;
import sesi.petvita.consultation.repository.ConsultationRepository;
import sesi.petvita.consultation.status.ConsultationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AppointmentReminderService {
    private final ConsultationRepository consultationRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    @Scheduled(cron = "0 0 8 * * *") // Roda todo dia às 8h
    public void sendAppointmentReminders() {
        System.out.println("[AGENDADOR] Tarefa iniciada em: " + LocalDateTime.now());
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<ConsultationModel> upcomingConsultations = consultationRepository
                .findAllByConsultationdateAndStatus(tomorrow, ConsultationStatus.AGENDADA);

        System.out.println("[AGENDADOR] Encontradas " + upcomingConsultations.size() + " consultas para amanhã.");

        if (upcomingConsultations.isEmpty()) {
            return;
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        for (ConsultationModel consultation : upcomingConsultations) {
            String message = "Lembrete: Você tem uma consulta para o pet " + consultation.getPet().getName() + " amanhã.";

            // CORREÇÃO AQUI: Passa o ID da consulta para a notificação
            notificationService.createNotification(consultation.getUsuario(), message, consultation.getId());

            Map<String, Object> emailModel = new HashMap<>();
            emailModel.put("titulo", "Lembrete de Consulta");
            emailModel.put("nomeUsuario", consultation.getUsuario().getActualUsername());
            emailModel.put("corpoMensagem", "Este é um lembrete amigável sobre sua consulta agendada para amanhã.");
            emailModel.put("mostrarDetalhesConsulta", true);
            emailModel.put("nomePet", consultation.getPet().getName());
            emailModel.put("nomeVeterinario", consultation.getVeterinario().getName());
            emailModel.put("dataConsulta", consultation.getConsultationdate().format(dateFormatter));
            emailModel.put("horarioConsulta", consultation.getConsultationtime().format(timeFormatter));

            emailService.sendHtmlEmailFromTemplate(consultation.getUsuario().getEmail(), "Lembrete de Consulta - Pet Vita", emailModel);
        }
    }
}