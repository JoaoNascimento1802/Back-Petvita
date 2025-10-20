package sesi.petvita.veterinary.model;

import jakarta.persistence.*;
import lombok.*;
import sesi.petvita.pet.model.MedicalRecord;

@Entity
@Table(name = "medical_attachments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalAttachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medical_record_id", nullable = false)
    private MedicalRecord medicalRecord;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false, length = 512)
    private String fileUrl;

    @Column(nullable = false)
    private String publicId; // Para deleção no Cloudinary
}