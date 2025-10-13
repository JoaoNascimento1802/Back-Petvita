package sesi.petvita.notification.model;

import jakarta.persistence.*;
import lombok.*;
import sesi.petvita.user.model.UserModel;
import java.time.LocalDateTime;

// NOVO ARQUIVO
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserModel user;

    @Column(nullable = false)
    private String message;

    private boolean isRead = false;

    private LocalDateTime createdAt;

    private Long consultationId;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}