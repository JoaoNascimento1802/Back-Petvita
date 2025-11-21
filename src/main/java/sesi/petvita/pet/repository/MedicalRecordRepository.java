package sesi.petvita.pet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sesi.petvita.pet.model.MedicalRecord;

import java.util.List;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    // Busca prontuários pelo ID do Pet (útil para a timeline)
    // Acessa o Pet através da Consulta: MedicalRecord -> Consultation -> Pet
    List<MedicalRecord> findByConsultation_Pet_IdOrderByCreatedAtDesc(Long petId);
}