package sesi.petvita.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.vaccine.model.VaccineModel;
import sesi.petvita.vaccine.repository.VaccineRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VaccineReminderService {

    private final VaccineRepository vaccineRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Executa todos os dias às 09:00 AM
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional(readOnly = true)
    public void sendVaccineReminders() {
        System.out.println("[VACINA] Verificando vacinas vencendo amanhã...");
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        List<VaccineModel> vaccinesDueTomorrow = vaccineRepository.findByNextDoseDate(tomorrow);

        for (VaccineModel vaccine : vaccinesDueTomorrow) {
            UserModel owner = vaccine.getPet().getUsuario();
            if (owner == null) continue;

            // 1. Notificação Interna
            String notificationMsg = "Lembrete: A vacina " + vaccine.getName() + " do pet " + vaccine.getPet().getName() + " vence amanhã (" + tomorrow.format(dateFormatter) + ").";
            notificationService.createNotification(owner, notificationMsg, null);

            // 2. E-mail
            try {
                Map<String, Object> emailModel = new HashMap<>();
                emailModel.put("titulo", "Lembrete de Vacinação");
                emailModel.put("nomeUsuario", owner.getActualUsername());
                emailModel.put("corpoMensagem", "A saúde do seu pet é importante! A próxima dose da vacina <strong>" + vaccine.getName() + "</strong> para o pet <strong>" + vaccine.getPet().getName() + "</strong> está prevista para amanhã, dia " + tomorrow.format(dateFormatter) + ".\n\nAgende uma consulta ou visite a clínica para manter a carteirinha em dia.");

                // Não mostra detalhes de consulta, pois é vacina
                emailModel.put("mostrarDetalhesConsulta", false);

                emailService.sendHtmlEmailFromTemplate(owner.getEmail(), "Lembrete de Vacina - Pet Vita", emailModel);
                System.out.println("[VACINA] E-mail enviado para " + owner.getEmail());
            } catch (Exception e) {
                System.err.println("[VACINA] Erro ao enviar e-mail para " + owner.getEmail() + ": " + e.getMessage());
            }
        }

        // Opcional: Poderíamos verificar vacinas vencidas há X dias também, mas vamos focar no preventivo (amanhã).
    }
}