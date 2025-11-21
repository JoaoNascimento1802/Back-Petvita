package sesi.petvita.veterinary.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import sesi.petvita.consultation.model.ConsultationModel;
import sesi.petvita.pet.model.MedicalRecord;

import java.time.LocalDateTime;

@Entity
@Table(name = "prescriptions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id", nullable = false)
    private ConsultationModel consultation;

    // CORREÇÃO: Removido o 'name="medication"' para usar o padrão 'medication_name'
    // O erro do banco dizia que 'medication_name' não tinha valor, então ele espera essa coluna.
    @Column(nullable = false, length = 100)
    private String medicationName;

    @Column(nullable = false, length = 100)
    private String dosage;

    @Column(nullable = false, length = 100)
    private String frequency;

    @Column(length = 100)
    private String duration;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_record_id")
    private MedicalRecord medicalRecord;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}