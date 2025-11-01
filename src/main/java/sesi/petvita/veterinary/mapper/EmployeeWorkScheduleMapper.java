package sesi.petvita.veterinary.mapper;

import org.springframework.stereotype.Component;
import sesi.petvita.veterinary.dto.EmployeeWorkScheduleDTO;
import sesi.petvita.veterinary.model.WorkSchedule;

@Component
public class EmployeeWorkScheduleMapper {

    public EmployeeWorkScheduleDTO toDTO(WorkSchedule model) {
        if (model == null) return null;

        Long empId = (model.getProfessionalUser() != null) ? model.getProfessionalUser().getId() : null;

        return new EmployeeWorkScheduleDTO(
                model.getId(),
                empId, // Mapeia para employeeId
                model.getDayOfWeek(),
                model.getStartTime(),
                model.getEndTime(),
                model.isWorking()
        );
    }
}