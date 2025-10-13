package sesi.petvita.consultation.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sesi.petvita.consultation.model.ConsultationModel;
import sesi.petvita.consultation.status.ConsultationStatus;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.veterinary.model.VeterinaryModel;
import sesi.petvita.veterinary.speciality.SpecialityEnum;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ConsultationRepository extends JpaRepository<ConsultationModel, Long> {

    @Query("SELECT c FROM ConsultationModel c WHERE " +
            "(:startDate IS NULL OR c.consultationdate >= :startDate) AND " +
            "(:endDate IS NULL OR c.consultationdate <= :endDate) AND " +
            "(:veterinaryId IS NULL OR c.veterinario.id = :veterinaryId) AND " +
            "(:speciality IS NULL OR c.specialityEnum = :speciality)")
    List<ConsultationModel> findWithFilters(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("veterinaryId") Long veterinaryId,
            @Param("speciality") SpecialityEnum speciality
    );

    @Query("SELECT c.consultationtime FROM ConsultationModel c WHERE c.veterinario.id = :veterinaryId AND c.consultationdate = :date AND c.status IN ('AGENDADA', 'PENDENTE')")
    List<LocalTime> findBookedTimesByVeterinarianAndDate(@Param("veterinaryId") Long veterinaryId, @Param("date") LocalDate date);

    List<ConsultationModel> findByVeterinarioAndConsultationdateBetween(VeterinaryModel vet, LocalDate startDate, LocalDate endDate);

    List<ConsultationModel> findByUsuarioAndConsultationdateBetween(UserModel user, LocalDate startDate, LocalDate endDate);

    List<ConsultationModel> findByConsultationdateBetween(LocalDate startDate, LocalDate endDate);

    List<ConsultationModel> findAllByConsultationdateAndStatus(LocalDate date, ConsultationStatus status);

    List<ConsultationModel> findByUsuarioId(Long usuarioId);

    List<ConsultationModel> findByVeterinarioIdAndConsultationdateAndConsultationtimeBetween(
            Long veterinarioId, LocalDate date, LocalTime startTime, LocalTime endTime
    );

    List<ConsultationModel> findByVeterinarioId(Long veterinarioId);

    List<ConsultationModel> findByVeterinarioIdAndSpecialityEnum(Long veterinarioId, SpecialityEnum specialityEnum);

    List<ConsultationModel> findByConsultationdate(LocalDate date);

    List<ConsultationModel> findBySpecialityEnum(SpecialityEnum speciality);

    List<ConsultationModel> findByVeterinario_NameContainingIgnoreCase(String veterinaryName);

    List<ConsultationModel> findByPet_NameContainingIgnoreCase(String petName);

    boolean existsByVeterinarioIdAndConsultationdateAndConsultationtime(Long veterinarioId, LocalDate date, LocalTime time);

    // ===== MÉTODO QUE ESTAVA FALTANDO, AGORA ADICIONADO =====
    boolean existsByVeterinarioIdAndStatusIn(Long veterinarioId, List<ConsultationStatus> statuses);
}