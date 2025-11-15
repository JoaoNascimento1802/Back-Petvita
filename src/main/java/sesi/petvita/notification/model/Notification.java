// sesi/petvita/notification/model/Notification.java
package sesi.petvita.notification.model;
import jakarta.persistence.*;
import lombok.*;
import sesi.petvita.user.model.UserModel;
import java.time.LocalDateTime;

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

    // --- CORREÇÃO AQUI ---
    // Alterado de FetchType.LAZY para FetchType.EAGER
    // Isso resolve o "Could not initialize proxy [sesi.petvita.user.model.UserModel#7] - no session"
    @ManyToOne(fetch = FetchType.EAGER)
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