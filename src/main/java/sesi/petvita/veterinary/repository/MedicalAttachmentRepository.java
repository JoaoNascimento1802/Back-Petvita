package sesi.petvita.veterinary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sesi.petvita.veterinary.model.MedicalAttachment;

import java.util.List;

@Repository
public interface MedicalAttachmentRepository extends JpaRepository<MedicalAttachment, Long> {

    /**
     * Encontra todos os anexos associados a um prontuário médico específico.
     * @param medicalRecordId O ID do prontuário médico.
     * @return Uma lista de anexos médicos.
     */
    List<MedicalAttachment> findByMedicalRecordId(Long medicalRecordId);
}