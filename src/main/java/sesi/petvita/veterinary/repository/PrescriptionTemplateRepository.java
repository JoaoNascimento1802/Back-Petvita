package sesi.petvita.veterinary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sesi.petvita.veterinary.model.PrescriptionTemplate;

import java.util.List;

@Repository
public interface PrescriptionTemplateRepository extends JpaRepository<PrescriptionTemplate, Long> {

    /**
     * Encontra todos os templates de prescrição criados por um usuário veterinário específico.
     * @param veterinaryUserId O ID da conta de usuário do veterinário.
     * @return Uma lista de templates de prescrição.
     */
    List<PrescriptionTemplate> findByVeterinaryUserId(Long veterinaryUserId);
}