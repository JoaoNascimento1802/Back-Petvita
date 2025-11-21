package sesi.petvita.vaccine.mapper;

import org.springframework.stereotype.Component;
import sesi.petvita.vaccine.dto.VaccineResponseDTO;
import sesi.petvita.vaccine.model.VaccineModel;

@Component
public class VaccineMapper {

    public VaccineResponseDTO toDTO(VaccineModel model) {
        return new VaccineResponseDTO(
                model.getId(),
                model.getName(),
                model.getManufacturer(),
                model.getBatch(),
                model.getApplicationDate(),
                model.getNextDoseDate(),
                model.getVeterinary() != null ? model.getVeterinary().getName() : "Aplicação Externa",
                model.getPet().getName(),
                model.getObservations()
        );
    }
}