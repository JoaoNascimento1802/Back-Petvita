package sesi.petvita.serviceschedule.mapper;

import org.springframework.stereotype.Component;
import sesi.petvita.clinic.model.ClinicService;
import sesi.petvita.pet.model.PetModel;
import sesi.petvita.serviceschedule.dto.ServiceScheduleRequestDTO;
import sesi.petvita.serviceschedule.dto.ServiceScheduleResponseDTO;
import sesi.petvita.serviceschedule.model.ServiceScheduleModel;
import sesi.petvita.user.model.UserModel;

@Component
public class ServiceScheduleMapper {

    public ServiceScheduleModel toModel(ServiceScheduleRequestDTO dto, PetModel pet, UserModel client, UserModel employee, ClinicService service) {
        return ServiceScheduleModel.builder()
                .pet(pet)
                .client(client)
                .employee(employee)
                .clinicService(service)
                .scheduleDate(dto.scheduleDate())
                .scheduleTime(dto.scheduleTime())
                .observations(dto.observations())
                // --- CORREÇÃO APLICADA AQUI ---
                // A linha ".status("AGENDADO")" foi removida.
                // Agora, o Builder usará o valor padrão ("PENDENTE") definido na entidade ServiceScheduleModel.
                .build();
    }

    public ServiceScheduleResponseDTO toDTO(ServiceScheduleModel model) {
        return new ServiceScheduleResponseDTO(
                model.getId(),
                model.getScheduleDate(),
                model.getScheduleTime(),
                model.getStatus(),
                model.getObservations(),
                model.getEmployeeReport(), // --- NOVO CAMPO ADICIONADO ---
                model.getPet().getId(),
                model.getPet().getName(),
                model.getClient().getId(),
                model.getClient().getActualUsername(),
                model.getEmployee().getId(),
                model.getEmployee().getActualUsername(),
                model.getClinicService().getId(),
                model.getClinicService().getName(),
                model.getClinicService().getPrice()
        );
    }
}