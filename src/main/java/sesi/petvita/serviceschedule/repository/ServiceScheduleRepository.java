package sesi.petvita.serviceschedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sesi.petvita.serviceschedule.model.ServiceScheduleModel;
import java.util.List;

@Repository
public interface ServiceScheduleRepository extends JpaRepository<ServiceScheduleModel, Long> {

    // Método para buscar todos os agendamentos de um determinado funcionário
    List<ServiceScheduleModel> findByEmployeeIdOrderByScheduleDateDescScheduleTimeDesc(Long employeeId);
}
