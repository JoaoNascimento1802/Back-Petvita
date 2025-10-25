package sesi.petvita.consultation.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import sesi.petvita.clinic.model.ClinicService;
import sesi.petvita.pet.model.PetModel;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.veterinary.model.VeterinaryModel;
import sesi.petvita.veterinary.speciality.SpecialityEnum;
import sesi.petvita.consultation.status.ConsultationStatus;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "consultations")
public class ConsultationModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate consultationdate;
    private LocalTime consultationtime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_service_id")
    private ClinicService clinicService;

    @Enumerated(EnumType.STRING)
    private SpecialityEnum specialityEnum;

    // --- GARANTIA DA REGRA DE NEGÓCIO CORRETA ---
    // Toda nova consulta começa como PENDENTE por padrão
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ConsultationStatus status = ConsultationStatus.PENDENTE;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String observations;

    @Column(columnDefinition = "TEXT")
    private String doctorReport;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserModel usuario;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id")
    private PetModel pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veterinary_id")
    private VeterinaryModel veterinario;
}