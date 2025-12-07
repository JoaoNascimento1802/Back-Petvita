package sesi.petvita.consultation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sesi.petvita.consultation.model.ConsultationModel;
import sesi.petvita.consultation.status.ConsultationStatus;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.veterinary.model.VeterinaryModel;
import sesi.petvita.veterinary.speciality.SpecialityEnum;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConsultationRepository extends JpaRepository<ConsultationModel, Long> {

    // Lista consultas do Usuário (Cliente) ordenadas
    List<ConsultationModel> findByUsuarioOrderByConsultationdateDesc(UserModel usuario);

    // Lista consultas do Veterinário ordenadas
    List<ConsultationModel> findByVeterinarioOrderByConsultationdateDesc(VeterinaryModel veterinario);

    // Detalhes da consulta com Fetch para performance
    @Query("SELECT c FROM ConsultationModel c " +
            "JOIN FETCH c.pet " +
            "JOIN FETCH c.usuario " +
            "JOIN FETCH c.veterinario v " +
            "LEFT JOIN FETCH v.userAccount " +
            "LEFT JOIN FETCH c.clinicService " +
            "WHERE c.id = :id")
    Optional<ConsultationModel> findByIdWithDetails(@Param("id") Long id);

    // Lista consultas com detalhes para o Usuário
    @Query("SELECT c FROM ConsultationModel c " +
            "JOIN FETCH c.pet " +
            "JOIN FETCH c.usuario " +
            "JOIN FETCH c.veterinario v " +
            "LEFT JOIN FETCH v.userAccount " +
            "LEFT JOIN FETCH c.clinicService " +
            "WHERE c.usuario.id = :usuarioId " +
            "ORDER BY c.consultationdate DESC, c.consultationtime DESC")
    List<ConsultationModel> findByUsuarioIdWithDetails(@Param("usuarioId") Long usuarioId);

    // Filtros (Admin/Relatórios)
    @Query("SELECT c FROM ConsultationModel c " +
            "JOIN FETCH c.pet " +
            "JOIN FETCH c.veterinario " +
            "WHERE " +
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

    // --- QUERY AJUSTADA: Busca horários ocupados ---
    // (Usa 'NOT IN' para garantir que FINALIZADA e CHECKED_IN também ocupem horário, não só AGENDADA)
    @Query("SELECT c.consultationtime FROM ConsultationModel c " +
            "WHERE c.veterinario.id = :veterinaryId " +
            "AND c.consultationdate = :date " +
            "AND c.status NOT IN ('CANCELADA', 'RECUSADA')")
    List<LocalTime> findBookedTimesByVeterinarianAndDate(@Param("veterinaryId") Long veterinaryId, @Param("date") LocalDate date);

    // --- QUERY NOVA: Validação para Edição (Evita conflito ignorando o próprio ID) ---
    boolean existsByVeterinarioIdAndConsultationdateAndConsultationtimeAndIdNot(
            Long veterinaryId,
            LocalDate date,
            LocalTime time,
            Long consultationId
    );

    // Métodos padrão do JPA (Mantidos conforme solicitado)
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

    List<ConsultationModel> findByConsultationdateAndStatusOrderByConsultationtimeAsc(LocalDate date, ConsultationStatus status);

    boolean existsByVeterinarioIdAndStatusIn(Long veterinarioId, List<ConsultationStatus> statuses);
}