// sesi/petvita/veterinary/model/VeterinaryModel.java
package sesi.petvita.veterinary.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.veterinary.speciality.SpecialityEnum;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "veterinary")
public class VeterinaryModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Dados específicos do veterinário
    private String crmv;

    @Enumerated(EnumType.STRING)
    private SpecialityEnum specialityenum;

    // Dados cadastrais
    private String name;
    private String phone;
    private String imageurl;

    // --- CAMPO QUE ESTAVA FALTANDO ---
    private String imagePublicId;
    // ---------------------------------

    // Relacionamento com a conta de usuário
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_account_id", referencedColumnName = "id")
    private UserModel userAccount;

    // Avaliações
    @JsonIgnore
    @OneToMany(mappedBy = "veterinary", cascade = CascadeType.ALL)
    private List<VeterinaryRating> ratings;

    private Double averageRating;
    private Integer ratingCount;
}