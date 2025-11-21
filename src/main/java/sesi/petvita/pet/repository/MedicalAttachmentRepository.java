package sesi.petvita.pet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sesi.petvita.pet.model.MedicalAttachment;

@Repository
public interface MedicalAttachmentRepository extends JpaRepository<MedicalAttachment, Long> {
}