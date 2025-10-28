package sesi.petvita.serviceschedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sesi.petvita.serviceschedule.model.ServiceScheduleModel;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ServiceScheduleRepository extends JpaRepository<ServiceScheduleModel, Long> {

    // --- CORREÇÃO PRINCIPAL AQUI ---
    // Este método substitui o findByEmployeeId... anterior.
    // Usamos JOIN FETCH para carregar todos os dados relacionados em uma única consulta,
    // o que previne a LazyInitializationException e melhora a performance.
    @Query("SELECT s FROM ServiceScheduleModel s " +
            "JOIN FETCH s.pet " +
            "JOIN FETCH s.client " +
            "JOIN FETCH s.employee " +
            "JOIN FETCH s.clinicService " +
            "WHERE s.employee.id = :employeeId " +
            "ORDER BY s.scheduleDate DESC, s.scheduleTime DESC")
    List<ServiceScheduleModel> findAllByEmployeeIdWithDetails(@Param("employeeId") Long employeeId);

    // Métodos existentes para o dashboard
    long countByEmployeeIdAndScheduleDate(Long employeeId, LocalDate date);
    long countByEmployeeIdAndStatusAndScheduleDateBetween(Long employeeId, String status, LocalDate startDate, LocalDate endDate);
    List<ServiceScheduleModel> findByClientIdOrderByScheduleDateDesc(Long clientId);
}
