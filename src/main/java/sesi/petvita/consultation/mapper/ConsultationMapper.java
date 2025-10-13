package sesi.petvita.consultation.mapper;

import org.springframework.stereotype.Component;
import sesi.petvita.clinic.model.ClinicService;
import sesi.petvita.consultation.dto.ConsultationRequestDTO;
import sesi.petvita.consultation.dto.ConsultationResponseDTO;
import sesi.petvita.consultation.model.ConsultationModel;
import sesi.petvita.consultation.status.ConsultationStatus;
import sesi.petvita.pet.model.PetModel;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.veterinary.model.VeterinaryModel;

@Component
public class ConsultationMapper {

    public ConsultationModel toModel(ConsultationRequestDTO dto, UserModel user, PetModel pet, VeterinaryModel vet, ClinicService service) {
        return ConsultationModel.builder()
                .consultationdate(dto.consultationdate())
                .consultationtime(dto.consultationtime())
                .clinicService(service) // Associa o serviço à consulta
                // CORREÇÃO AQUI: A especialidade é derivada do VETERINÁRIO, não do serviço.
                .specialityEnum(vet.getSpecialityenum())
                .status(ConsultationStatus.PENDENTE)
                .reason(dto.reason())
                .observations(dto.observations())
                .usuario(user)
                .pet(pet)
                .veterinario(vet)
                .build();
    }

    public ConsultationResponseDTO toDTO(ConsultationModel model) {
        return new ConsultationResponseDTO(
                model.getId(),
                model.getConsultationdate(),
                model.getConsultationtime(),
                model.getSpecialityEnum().getDescricao(),
                model.getStatus(),
                model.getReason(),
                model.getObservations(),
                model.getPet() != null ? model.getPet().getName() : "N/A",
                model.getVeterinario() != null ? model.getVeterinario().getName() : "N/A",
                model.getUsuario() != null ? model.getUsuario().getId() : null,
                model.getUsuario() != null ? model.getUsuario().getActualUsername() : "N/A",
                model.getVeterinario() != null ? model.getVeterinario().getId() : null,
                model.getClinicService() != null ? model.getClinicService().getName() : "N/A",
                model.getClinicService() != null ? model.getClinicService().getPrice() : null
        );
    }
}