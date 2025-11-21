package sesi.petvita.pet.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sesi.petvita.pet.dto.MedicalRecordResponseDTO;
import sesi.petvita.pet.mapper.MedicalRecordMapper;
import sesi.petvita.pet.model.MedicalRecord;
import sesi.petvita.pet.repository.MedicalRecordRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final MedicalRecordMapper medicalRecordMapper;

    @Transactional(readOnly = true)
    public List<MedicalRecordResponseDTO> findRecordsByPet(Long petId) {
        return medicalRecordRepository.findByConsultation_Pet_IdOrderByCreatedAtDesc(petId)
                .stream()
                .map(medicalRecordMapper::toDTO)
                .collect(Collectors.toList());
    }

    // --- NOVO MÉTODO ---
    @Transactional(readOnly = true)
    public MedicalRecordResponseDTO findById(Long id) {
        MedicalRecord record = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Prontuário não encontrado com o ID: " + id));
        return medicalRecordMapper.toDTO(record);
    }
}