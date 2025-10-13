package sesi.petvita.clinic.service;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sesi.petvita.clinic.model.ClinicService;
import sesi.petvita.clinic.repository.ClinicServiceRepository;
import sesi.petvita.veterinary.speciality.SpecialityEnum;

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

    public ClinicService save(ClinicService clinicService) {
        // Validações adicionais podem ser inseridas aqui antes de salvar
        return clinicServiceRepository.save(clinicService);
    }

    public ClinicService update(Long id, ClinicService serviceDetails) {
        ClinicService existingService = findById(id);
        existingService.setName(serviceDetails.getName());
        existingService.setDescription(serviceDetails.getDescription());
        existingService.setPrice(serviceDetails.getPrice());
        return clinicServiceRepository.save(existingService);
    }

    public void delete(Long id) {
        if (!clinicServiceRepository.existsById(id)) {
            throw new NoSuchElementException("Serviço não encontrado com o ID: " + id);
        }
        clinicServiceRepository.deleteById(id);
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpecialityEnum speciality;
}