package sesi.petvita.consultation.mapper;

import org.springframework.stereotype.Component;
import sesi.petvita.clinic.model.ClinicService;
import sesi.petvita.consultation.dto.ConsultationRequestDTO;
import sesi.petvita.consultation.dto.ConsultationResponseDTO;
import sesi.petvita.consultation.model.ConsultationModel;
import sesi.petvita.pet.model.PetModel;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.veterinary.model.VeterinaryModel;

@Component
public class ConsultationMapper {

    public ConsultationModel toModel(ConsultationRequestDTO requestDTO, PetModel pet, UserModel user, VeterinaryModel vet, ClinicService service) {
        return ConsultationModel.builder()
                .consultationdate(requestDTO.consultationdate())
                .consultationtime(requestDTO.consultationtime())
                .reason(requestDTO.reason())
                .observations(requestDTO.observations())
                .usuario(user)
                .pet(pet)
                .veterinario(vet)
                .clinicService(service)
                .specialityEnum(vet.getSpecialityenum())
                .build();
    }

    public ConsultationResponseDTO toDTO(ConsultationModel model) {
        Long recordId = (model.getMedicalRecord() != null) ? model.getMedicalRecord().getId() : null;
        
        return new ConsultationResponseDTO(
                model.getId(),
                model.getChatRoomId(), // <--- MAPEANDO O NOVO CAMPO
                model.getConsultationdate(),
                model.getConsultationtime(),
                model.getStatus(),
                model.getReason(),
                model.getObservations(),
                model.getDoctorReport(),
                model.getPet().getId(),
                model.getPet().getName(),
                model.getPet().getImageurl(),
                model.getUsuario().getId(),
                model.getUsuario().getActualUsername(),
                model.getVeterinario().getId(),
                model.getVeterinario().getName(),
                model.getClinicService().getName(),
                model.getClinicService().getPrice(),
                model.getSpecialityEnum(),
                recordId
        );
    }
}
