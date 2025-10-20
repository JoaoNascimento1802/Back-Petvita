package sesi.petvita.veterinary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sesi.petvita.veterinary.model.Prescription;

import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    /**
     * Encontra todas as prescrições associadas a uma consulta específica.
     * @param consultationId O ID da consulta.
     * @return Uma lista de prescrições para a consulta.
     */
    List<Prescription> findByConsultationId(Long consultationId);

    /**
     * Encontra a prescrição mais recente para uma determinada consulta.
     * @param consultationId O ID da consulta.
     * @return Um Optional contendo a prescrição mais recente, se houver.
     */
    Optional<Prescription> findTopByConsultationIdOrderByCreatedAtDesc(Long consultationId);
}