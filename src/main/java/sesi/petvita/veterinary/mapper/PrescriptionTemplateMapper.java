package sesi.petvita.veterinary.mapper;

import org.springframework.stereotype.Component;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.veterinary.dto.PrescriptionTemplateRequestDTO;
import sesi.petvita.veterinary.dto.PrescriptionTemplateResponseDTO;
import sesi.petvita.veterinary.model.PrescriptionTemplate;

@Component
public class PrescriptionTemplateMapper {

    public PrescriptionTemplate toModel(PrescriptionTemplateRequestDTO dto, UserModel user) {
        return PrescriptionTemplate.builder()
                .title(dto.title())
                .medication(dto.medication())
                .dosage(dto.dosage())
                .instructions(dto.instructions())
                .veterinaryUser(user)
                .build();
    }

    public PrescriptionTemplateResponseDTO toDTO(PrescriptionTemplate model) {
        return new PrescriptionTemplateResponseDTO(
                model.getId(),
                model.getTitle(),
                model.getMedication(),
                model.getDosage(),
                model.getInstructions()
        );
    }
}