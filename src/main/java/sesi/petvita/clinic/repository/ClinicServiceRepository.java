package sesi.petvita.clinic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sesi.petvita.clinic.model.ClinicService;

import java.util.Optional;

@Repository
public interface ClinicServiceRepository extends JpaRepository<ClinicService, Long> {
    // Método necessário para verificar se o serviço já existe pelo nome
    Optional<ClinicService> findByName(String name);
}