package sesi.petvita.clinic.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sesi.petvita.clinic.dto.ClinicServiceRequestDTO;
import sesi.petvita.clinic.model.ClinicService;
import sesi.petvita.clinic.repository.ClinicServiceRepository;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ClinicServiceService {

    private final ClinicServiceRepository clinicServiceRepository;

    public List<ClinicService> findAll() {
        return clinicServiceRepository.findAll();
    }

    public ClinicService findById(Long id) {
        return clinicServiceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Serviço não encontrado com o ID: " + id));
    }

    public ClinicService save(ClinicServiceRequestDTO dto) {
        ClinicService newService = new ClinicService();
        newService.setName(dto.name());
        newService.setDescription(dto.description());
        newService.setPrice(dto.price());
        // Esta linha só compila se o Lombok estiver a funcionar corretamente na IDE
        newService.setSpeciality(dto.speciality());
        newService.setMedicalService(dto.isMedicalService());
        return clinicServiceRepository.save(newService);
    }

    public ClinicService update(Long id, ClinicServiceRequestDTO dto) {
        ClinicService existingService = findById(id);
        existingService.setName(dto.name());
        existingService.setDescription(dto.description());
        existingService.setPrice(dto.price());
        // E esta linha também
        existingService.setSpeciality(dto.speciality());
        existingService.setMedicalService(dto.isMedicalService());
        return clinicServiceRepository.save(existingService);
    }

    public void delete(Long id) {
        if (!clinicServiceRepository.existsById(id)) {
            throw new NoSuchElementException("Serviço não encontrado com o ID: " + id);
        }
        clinicServiceRepository.deleteById(id);
    }
}