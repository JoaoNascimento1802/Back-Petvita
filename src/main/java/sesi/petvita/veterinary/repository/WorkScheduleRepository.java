package sesi.petvita.veterinary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sesi.petvita.veterinary.model.WorkSchedule;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Long> {

    // --- 1. MÉTODOS PARA O PADRÃO SEMANAL (Templates) ---
    // Importante: Filtra por 'workDate IS NULL' para pegar apenas a configuração fixa.
    // O nome 'findByProfessionalUserId' é mantido para compatibilidade com códigos antigos.

    @Query("SELECT ws FROM WorkSchedule ws WHERE ws.professionalUser.id = :userId AND ws.workDate IS NULL ORDER BY ws.dayOfWeek")
    List<WorkSchedule> findByProfessionalUserId(@Param("userId") Long userId);

    @Query("SELECT ws FROM WorkSchedule ws WHERE ws.professionalUser.id = :userId AND ws.dayOfWeek = :dayOfWeek AND ws.workDate IS NULL")
    Optional<WorkSchedule> findByProfessionalUserIdAndDayOfWeek(@Param("userId") Long userId, @Param("dayOfWeek") DayOfWeek dayOfWeek);



    // --- 2. MÉTODOS PARA DATAS ESPECÍFICAS (Exceções/Calendário) ---

    // Busca um horário específico para uma data exata
    Optional<WorkSchedule> findByProfessionalUserIdAndWorkDate(Long userId, LocalDate workDate);

    // Busca todas as exceções futuras (usado na lista de exceções)
    @Query("SELECT ws FROM WorkSchedule ws WHERE ws.professionalUser.id = :userId AND ws.workDate >= :today ORDER BY ws.workDate")
    List<WorkSchedule> findSpecificSchedulesByUserId(@Param("userId") Long userId, @Param("today") LocalDate today);

    // Busca exceções dentro de um intervalo (usado para montar o calendário visual)
    @Query("SELECT ws FROM WorkSchedule ws WHERE ws.professionalUser.id = :userId AND ws.workDate BETWEEN :startDate AND :endDate")
    List<WorkSchedule> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}