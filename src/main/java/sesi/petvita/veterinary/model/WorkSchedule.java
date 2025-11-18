// sesi/petvita/veterinary/model/WorkSchedule.java
package sesi.petvita.veterinary.model;

import jakarta.persistence.*;
import lombok.*;
import sesi.petvita.user.model.UserModel;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "work_schedule")
public class WorkSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private DayOfWeek dayOfWeek;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "is_working")
    private boolean isWorking;

    // CORREÇÃO: Liga o horário ao Usuário Profissional (Vet ou Employee)
    // Isso substitui o antigo "veterinary_id" que estava dando erro
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_user_id", nullable = false)
    private UserModel professionalUser;
}