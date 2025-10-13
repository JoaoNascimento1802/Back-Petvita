package sesi.petvita.notification.model;

import jakarta.persistence.*;
import lombok.*;
import sesi.petvita.consultation.model.ConsultationModel;
import sesi.petvita.user.model.UserModel;
import java.time.LocalDateTime;

// NOVO ARQUIVO
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "consultation_id", nullable = false)
    private ConsultationModel consultation;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private UserModel sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private UserModel receiver;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private LocalDateTime sentAt;

    @PrePersist
    protected void onSent() {
        sentAt = LocalDateTime.now();
    }
}