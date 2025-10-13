package sesi.petvita.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendHtmlEmailFromTemplate(String to, String subject, Map<String, Object> templateModel) {
        try {
            Context context = new Context();
            context.setVariables(templateModel);

            String htmlBody = templateEngine.process("email-template.html", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();

            // CORREÇÃO AQUI: A ordem dos parâmetros foi ajustada para (MimeMessage, multipart, encoding)
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(mimeMessage);
            System.out.println("E-mail com template HTML enviado com sucesso para: " + to);
        } catch (MessagingException e) {
            System.err.println("Erro ao enviar e-mail com template para " + to + ": " + e.getMessage());
        }
    }
}