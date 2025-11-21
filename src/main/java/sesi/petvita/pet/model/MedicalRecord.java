package sesi.petvita.pet.model;

import jakarta.persistence.*;
import lombok.*;
import sesi.petvita.consultation.model.ConsultationModel;
import sesi.petvita.veterinary.model.Prescription;
import sesi.petvita.veterinary.model.VeterinaryModel;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "medical_record")
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "record_date", nullable = false)
    private LocalDateTime recordDate;

    @Column(columnDefinition = "TEXT")
    private String diagnosis;

    @Column(columnDefinition = "TEXT")
    private String treatment;

    // --- CORREÇÃO CRÍTICA ---
    // O banco exige pet_id. Mapeamos aqui.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private PetModel pet;
    // ------------------------

    @OneToOne
    @JoinColumn(name = "consultation_id")
    private ConsultationModel consultation;

    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL)
    private List<MedicalAttachment> attachments;

    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL)
    private List<Prescription> prescriptions;

    @ManyToOne
    @JoinColumn(name = "veterinary_id")
    private VeterinaryModel veterinary;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.recordDate = now;
    }
}