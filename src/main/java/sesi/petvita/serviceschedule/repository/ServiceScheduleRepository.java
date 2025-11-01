package sesi.petvita.serviceschedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sesi.petvita.serviceschedule.model.ServiceScheduleModel;

import java.time.LocalDate;
import java.time.LocalTime; // <-- IMPORTAR
import java.util.List;

@Repository
public interface ServiceScheduleRepository extends JpaRepository<ServiceScheduleModel, Long> {

    @Query("SELECT s FROM ServiceScheduleModel s " +
            "JOIN FETCH s.pet " +
            "JOIN FETCH s.client " +
            "JOIN FETCH s.employee " +
            "JOIN FETCH s.clinicService " +
            "WHERE s.employee.id = :employeeId " +
            "ORDER BY s.scheduleDate DESC, s.scheduleTime DESC")
    List<ServiceScheduleModel> findAllByEmployeeIdWithDetails(@Param("employeeId") Long employeeId);

    long countByEmployeeIdAndScheduleDate(Long employeeId, LocalDate date);

    long countByEmployeeIdAndStatusAndScheduleDateBetween(Long employeeId, String status, LocalDate startDate, LocalDate endDate);

    List<ServiceScheduleModel> findByClientIdOrderByScheduleDateDesc(Long clientId);

    // --- NOVO MÉTODO ADICIONADO AQUI ---
    /**
     * Busca os horários de início de serviços PENDENTES ou AGENDADOS
     * para um funcionário específico em uma data específica.
     */
    @Query("SELECT s.scheduleTime FROM ServiceScheduleModel s " +
            "WHERE s.employee.id = :employeeId " +
            "AND s.scheduleDate = :date " +
            "AND s.status IN ('AGENDADO', 'PENDENTE')")
    List<LocalTime> findBookedTimesByEmployeeAndDate(@Param("employeeId") Long employeeId, @Param("date") LocalDate date);
}