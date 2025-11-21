package sesi.petvita.vaccine.model;

import jakarta.persistence.*;
import lombok.*;
import sesi.petvita.pet.model.PetModel;
import sesi.petvita.veterinary.model.VeterinaryModel;

import java.time.LocalDate;

@Entity
@Table(name = "vaccines")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VaccineModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // Ex: V10, Antirrábica

    private String manufacturer; // Fabricante (ex: Zoetis, Vanguard)
    private String batch;        // Lote

    @Column(nullable = false)
    private LocalDate applicationDate; // Data da aplicação

    private LocalDate nextDoseDate; // Data da próxima dose (calculada ou manual)

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pet_id", nullable = false)
    private PetModel pet;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "veterinary_id")
    private VeterinaryModel veterinary; // Quem aplicou (pode ser nulo se for histórico antigo importado)

    private String observations;
}