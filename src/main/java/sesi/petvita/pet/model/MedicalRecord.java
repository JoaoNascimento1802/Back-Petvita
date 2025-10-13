package sesi.petvita.pet.model;

import jakarta.persistence.*;
import lombok.*;
import sesi.petvita.consultation.model.ConsultationModel;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private PetModel pet;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id")
    private ConsultationModel consultation;

    @Column(nullable = false)
    private LocalDateTime recordDate;

    @Column(columnDefinition = "TEXT")
    private String anamnese; // Queixas do tutor e observações do vet

    @Column(columnDefinition = "TEXT")
    private String diagnosis; // Diagnóstico

    @Column(columnDefinition = "TEXT")
    private String treatment; // Tratamento prescrito

    @PrePersist
    protected void onCreate() {
        recordDate = LocalDateTime.now();
    }
}