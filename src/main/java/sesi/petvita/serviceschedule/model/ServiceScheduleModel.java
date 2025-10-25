package sesi.petvita.serviceschedule.model;

import jakarta.persistence.*;
import lombok.*;
import sesi.petvita.clinic.model.ClinicService;
import sesi.petvita.pet.model.PetModel;
import sesi.petvita.user.model.UserModel;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "service_schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceScheduleModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private PetModel pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private UserModel client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private UserModel employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_service_id", nullable = false)
    private ClinicService clinicService;

    @Column(nullable = false)
    private LocalDate scheduleDate;

    @Column(nullable = false)
    private LocalTime scheduleTime;

    @Column(columnDefinition = "TEXT")
    private String observations;

    @Builder.Default
    private String status = "PENDENTE";

    // --- NOVO CAMPO ADICIONADO ---
    @Column(columnDefinition = "TEXT")
    private String employeeReport;
}