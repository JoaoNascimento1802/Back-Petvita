package sesi.petvita.consultation.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Length;
import sesi.petvita.clinic.model.ClinicService;
import sesi.petvita.consultation.status.ConsultationStatus;
import sesi.petvita.notification.model.ChatMessage;
import sesi.petvita.pet.model.PetModel;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.veterinary.model.VeterinaryModel;
import sesi.petvita.veterinary.speciality.SpecialityEnum;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "consultas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationModel {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @JsonFormat(pattern = "yyyy-MM-dd")
        @Column(nullable = false)
        private LocalDate consultationdate;

        @JsonFormat(pattern = "HH:mm")
        @Column(nullable = false)
        private LocalTime consultationtime;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private SpecialityEnum specialityEnum;

        @Column(nullable = false)
        private String observations;

        @JsonBackReference
        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "pet_id")
        private PetModel pet;

        @JsonBackReference
        @ManyToOne
        @JoinColumn(name = "usuario_id")
        private UserModel usuario;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false , length = 50)
        @Builder.Default
        private ConsultationStatus status = ConsultationStatus.PENDENTE;

        @Column(nullable = false, columnDefinition = "TEXT")
        private String reason;

        @Column(columnDefinition = "TEXT")
        private String doctorReport;

        // ===== ALTERAÇÃO AQUI: Adicione fetch = FetchType.EAGER =====
        @OneToMany(mappedBy = "consultation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
        private List<ChatMessage> chatMessages;
        // ==========================================================

        private LocalDateTime dataCriacao;
        private LocalDateTime dataAtualizacao;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "clinic_service_id", nullable = false)
    private ClinicService clinicService;

        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "veterinario_id")
        private VeterinaryModel veterinario;

        @PrePersist
        public void prePersist() {
                this.dataCriacao = LocalDateTime.now();
        }

        @PreUpdate
        public void preUpdate() {
                this.dataAtualizacao = LocalDateTime.now();
        }
}