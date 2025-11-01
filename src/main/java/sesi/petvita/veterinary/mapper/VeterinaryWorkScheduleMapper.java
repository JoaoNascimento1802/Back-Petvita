package sesi.petvita.veterinary.mapper;

import org.springframework.stereotype.Component;
import sesi.petvita.veterinary.dto.VeterinaryWorkScheduleDTO;
import sesi.petvita.veterinary.model.WorkSchedule;

@Component
public class VeterinaryWorkScheduleMapper {

    public VeterinaryWorkScheduleDTO toDTO(WorkSchedule model) {
        if (model == null) return null;

        Long vetUserId = (model.getProfessionalUser() != null) ? model.getProfessionalUser().getId() : null;

        return new VeterinaryWorkScheduleDTO(
                model.getId(),
                vetUserId, // Mapeia para veterinaryId
                model.getDayOfWeek(),
                model.getStartTime(),
                model.getEndTime(),
                model.isWorking()
        );
    }
}