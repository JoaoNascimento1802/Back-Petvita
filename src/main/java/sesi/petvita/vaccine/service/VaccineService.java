package sesi.petvita.vaccine.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sesi.petvita.pet.model.PetModel;
import sesi.petvita.pet.repository.PetRepository;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.user.role.UserRole;
import sesi.petvita.vaccine.dto.VaccineRequestDTO;
import sesi.petvita.vaccine.dto.VaccineResponseDTO;
import sesi.petvita.vaccine.mapper.VaccineMapper;
import sesi.petvita.vaccine.model.VaccineModel;
import sesi.petvita.vaccine.repository.VaccineRepository;
import sesi.petvita.veterinary.model.VeterinaryModel;
import sesi.petvita.veterinary.repository.VeterinaryRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VaccineService {

    private final VaccineRepository vaccineRepository;
    private final PetRepository petRepository;
    private final VeterinaryRepository veterinaryRepository;
    private final VaccineMapper vaccineMapper;

    @Transactional
    public VaccineResponseDTO addVaccine(VaccineRequestDTO dto, UserModel user) {
        // 1. Validação de Permissão
        if (user.getRole() != UserRole.VETERINARY) {
            throw new IllegalStateException("Apenas veterinários podem registrar vacinas.");
        }

        // 2. Buscar o Pet
        PetModel pet = petRepository.findById(dto.petId())
                .orElseThrow(() -> new NoSuchElementException("Pet não encontrado com o ID: " + dto.petId()));

        // 3. Buscar o Perfil do Veterinário logado
        VeterinaryModel vet = veterinaryRepository.findByUserAccount(user)
                .orElseThrow(() -> new IllegalStateException("Perfil de veterinário não encontrado para este usuário."));

        // 4. Validação de Datas
        if (dto.nextDoseDate() != null && dto.nextDoseDate().isBefore(dto.applicationDate())) {
            throw new IllegalStateException("A data da próxima dose não pode ser anterior à data de aplicação.");
        }

        // 5. Criação da Vacina
        VaccineModel vaccine = VaccineModel.builder()
                .name(dto.name())
                .manufacturer(dto.manufacturer())
                .batch(dto.batch())
                .applicationDate(dto.applicationDate())
                .nextDoseDate(dto.nextDoseDate()) // Pode ser nulo (dose única)
                .observations(dto.observations())
                .pet(pet)
                .veterinary(vet) // Vincula quem aplicou
                .build();

        VaccineModel savedVaccine = vaccineRepository.save(vaccine);
        return vaccineMapper.toDTO(savedVaccine);
    }

    @Transactional(readOnly = true)
    public List<VaccineResponseDTO> getVaccinesByPet(Long petId) {
        if (!petRepository.existsById(petId)) {
            throw new NoSuchElementException("Pet não encontrado com o ID: " + petId);
        }
        return vaccineRepository.findByPetIdOrderByApplicationDateDesc(petId)
                .stream()
                .map(vaccineMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteVaccine(Long vaccineId, UserModel user) {
        VaccineModel vaccine = vaccineRepository.findById(vaccineId)
                .orElseThrow(() -> new NoSuchElementException("Registro de vacina não encontrado."));

        // Permite que Admin ou Veterinário excluam
        if (user.getRole() != UserRole.VETERINARY && user.getRole() != UserRole.ADMIN) {
            throw new IllegalStateException("Você não tem permissão para excluir este registro.");
        }

        // Opcional: Restringir para que apenas o Vet que criou possa excluir
        // if (user.getRole() == UserRole.VETERINARY) {
        //     VeterinaryModel vet = veterinaryRepository.findByUserAccount(user).orElseThrow();
        //     if (!vaccine.getVeterinary().getId().equals(vet.getId())) {
        //         throw new IllegalStateException("Você só pode excluir registros criados por você.");
        //     }
        // }

        vaccineRepository.deleteById(vaccineId);
    }

    @Transactional
    public VaccineResponseDTO updateVaccine(Long id, VaccineRequestDTO dto, UserModel user) {
        if (user.getRole() != UserRole.VETERINARY) {
            throw new IllegalStateException("Apenas veterinários podem editar vacinas.");
        }

        VaccineModel vaccine = vaccineRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Vacina não encontrada"));

        // Validação de Datas
        if (dto.nextDoseDate() != null && dto.nextDoseDate().isBefore(dto.applicationDate())) {
            throw new IllegalStateException("A data da próxima dose não pode ser anterior à data de aplicação.");
        }

        vaccine.setName(dto.name());
        vaccine.setManufacturer(dto.manufacturer());
        vaccine.setBatch(dto.batch());
        vaccine.setApplicationDate(dto.applicationDate());
        vaccine.setNextDoseDate(dto.nextDoseDate());
        vaccine.setObservations(dto.observations());

        return vaccineMapper.toDTO(vaccineRepository.save(vaccine));
    }
}