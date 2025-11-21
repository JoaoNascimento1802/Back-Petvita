package sesi.petvita.pet.dto;

import lombok.Data;
import sesi.petvita.veterinary.model.Prescription;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MedicalRecordResponseDTO {
    private Long id;
    private LocalDateTime createdAt;
    private String diagnosis;
    private String treatment;
    private String veterinaryName;
    private List<AttachmentDTO> attachments;
    private List<PrescriptionDTO> prescriptions;

    @Data
    public static class AttachmentDTO {
        private String fileName;
        private String fileUrl;
    }

    @Data
    public static class PrescriptionDTO {
        private String medicationName;
        private String dosage;
        private String frequency;
        private String duration;
    }
}