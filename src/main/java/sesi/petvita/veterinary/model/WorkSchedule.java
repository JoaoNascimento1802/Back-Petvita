package sesi.petvita.veterinary.model;

import jakarta.persistence.*;
import lombok.*;
import sesi.petvita.user.model.UserModel; // Importar UserModel
import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ALTERAÇÃO: Referenciar UserModel diretamente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_user_id", nullable = false)
    private UserModel professionalUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    private boolean isWorking;
}