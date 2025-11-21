package sesi.petvita.pet.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String fileUrl; // URL do Cloudinary
    private String fileType; // "PDF", "IMAGE", etc.

    // ID Público do Cloudinary para poder deletar depois
    private String publicId;

    @ManyToOne
    @JoinColumn(name = "medical_record_id")
    private MedicalRecord medicalRecord;
}