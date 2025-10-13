package sesi.petvita.veterinary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sesi.petvita.veterinary.model.WorkSchedule;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Long> {
    List<WorkSchedule> findByVeterinaryId(Long veterinaryId);
    Optional<WorkSchedule> findByVeterinaryIdAndDayOfWeek(Long veterinaryId, DayOfWeek dayOfWeek);
}