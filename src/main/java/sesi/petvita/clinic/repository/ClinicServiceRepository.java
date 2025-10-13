package sesi.petvita.clinic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sesi.petvita.clinic.model.ClinicService;

@Repository
public interface ClinicServiceRepository extends JpaRepository<ClinicService, Long> {
}