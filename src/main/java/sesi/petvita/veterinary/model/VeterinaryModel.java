package sesi.petvita.veterinary.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import sesi.petvita.consultation.model.ConsultationModel;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.veterinary.speciality.SpecialityEnum;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "veterinary")
public class VeterinaryModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(nullable = false)
    private String name;

    // O CAMPO 'password' FOI REMOVIDO DAQUI
    // O CAMPO 'email' FOI REMOVIDO DAQUI

    @Size(min = 6, max = 15, message = "O CRM deve ter entre 6 e 15 caracteres.")
    @Pattern(regexp = "^[A-Za-z]{2}\\s?\\d+$", message = "Formato de CRM inválido. Ex: SP 123456 ou SP123456")
    @Column(nullable = false, unique = true) // CRMV deve ser único
    private String crmv;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpecialityEnum specialityenum;

    @NotBlank
    @Column(nullable = false)
    private String phone;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_account_id")
    private UserModel userAccount;

    @NotBlank
    @Column(nullable = false)
    private String imageurl;

    private String imagePublicId;

    @OneToMany(mappedBy = "veterinario")
    private List<ConsultationModel> consultas;

    @Builder.Default
    private Double averageRating = 0.0;

    @Builder.Default
    private Integer ratingCount = 0;

    @OneToMany(mappedBy = "veterinary", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VeterinaryRating> ratings;
}