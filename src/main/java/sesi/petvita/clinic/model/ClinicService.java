package sesi.petvita.clinic.model;

import jakarta.persistence.*;
import lombok.*;
import sesi.petvita.veterinary.speciality.SpecialityEnum;

import java.math.BigDecimal;

@Entity
@Getter // O Lombok cria os métodos get...()
@Setter // O Lombok cria os métodos set...(), incluindo setSpeciality()
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClinicService {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpecialityEnum speciality;

    @Column(name = "is_medical_service", nullable = false)
    private boolean isMedicalService;
}