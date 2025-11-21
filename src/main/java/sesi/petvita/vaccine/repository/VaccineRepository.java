package sesi.petvita.vaccine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sesi.petvita.vaccine.model.VaccineModel;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VaccineRepository extends JpaRepository<VaccineModel, Long> {
    // Busca todas as vacinas de um pet
    List<VaccineModel> findByPetIdOrderByApplicationDateDesc(Long petId);

    // NOVO: Busca vacinas cuja próxima dose é em uma data específica
    List<VaccineModel> findByNextDoseDate(LocalDate nextDoseDate);
}