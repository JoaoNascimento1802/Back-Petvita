package sesi.petvita.veterinary.model;

import jakarta.persistence.*;
import lombok.*;
import sesi.petvita.user.model.UserModel;

@Entity
@Table(name = "prescription_templates")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veterinary_user_id", nullable = false)
    private UserModel veterinaryUser; // Linkado à conta de usuário do veterinário

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String medication;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String dosage;

    @Column(columnDefinition = "TEXT")
    private String instructions;
}