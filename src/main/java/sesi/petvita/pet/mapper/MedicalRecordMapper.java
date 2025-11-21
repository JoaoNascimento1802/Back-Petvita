package sesi.petvita.pet.mapper;

import org.springframework.stereotype.Component;
import sesi.petvita.pet.dto.MedicalRecordResponseDTO;
import sesi.petvita.pet.model.MedicalAttachment;
import sesi.petvita.pet.model.MedicalRecord;
import sesi.petvita.veterinary.model.Prescription;

import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class MedicalRecordMapper {

    public MedicalRecordResponseDTO toDTO(MedicalRecord record) {
        if (record == null) {
            return null;
        }

        MedicalRecordResponseDTO dto = new MedicalRecordResponseDTO();
        dto.setId(record.getId());
        dto.setCreatedAt(record.getCreatedAt());
        dto.setDiagnosis(record.getDiagnosis());
        dto.setTreatment(record.getTreatment());

        if (record.getVeterinary() != null) {
            dto.setVeterinaryName(record.getVeterinary().getName());
        } else {
            dto.setVeterinaryName("Não informado");
        }

        if (record.getAttachments() != null) {
            dto.setAttachments(record.getAttachments().stream().map(this::toAttachmentDTO).collect(Collectors.toList()));
        } else {
            dto.setAttachments(Collections.emptyList());
        }

        if (record.getPrescriptions() != null) {
            dto.setPrescriptions(record.getPrescriptions().stream().map(this::toPrescriptionDTO).collect(Collectors.toList()));
        } else {
            dto.setPrescriptions(Collections.emptyList());
        }

        return dto;
    }

    private MedicalRecordResponseDTO.AttachmentDTO toAttachmentDTO(MedicalAttachment attachment) {
        if (attachment == null) {
            return null;
        }
        MedicalRecordResponseDTO.AttachmentDTO dto = new MedicalRecordResponseDTO.AttachmentDTO();
        dto.setFileName(attachment.getFileName());
        dto.setFileUrl(attachment.getFileUrl());
        return dto;
    }

    private MedicalRecordResponseDTO.PrescriptionDTO toPrescriptionDTO(Prescription prescription) {
        if (prescription == null) {
            return null;
        }
        MedicalRecordResponseDTO.PrescriptionDTO dto = new MedicalRecordResponseDTO.PrescriptionDTO();
        dto.setMedicationName(prescription.getMedicationName());
        dto.setDosage(prescription.getDosage());
        dto.setFrequency(prescription.getFrequency());
        dto.setDuration(prescription.getDuration());
        return dto;
    }
}