package sesi.petvita.clinic.model;

import jakarta.persistence.*;
import lombok.*;
import sesi.petvita.veterinary.speciality.SpecialityEnum;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
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
    @Column(nullable = false, length = 50)
    private SpecialityEnum speciality;


    @Column(name = "is_medical_service", nullable = false)
    private boolean isMedicalService;
}