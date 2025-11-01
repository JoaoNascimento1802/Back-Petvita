package sesi.petvita.veterinary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sesi.petvita.veterinary.model.WorkSchedule;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, Long> {

    // ALTERAÇÃO: Buscar pelo ID do UserModel
    @Query("SELECT ws FROM WorkSchedule ws WHERE ws.professionalUser.id = :userId ORDER BY ws.dayOfWeek")
    List<WorkSchedule> findByProfessionalUserId(@Param("userId") Long userId);

    @Query("SELECT ws FROM WorkSchedule ws WHERE ws.professionalUser.id = :userId AND ws.dayOfWeek = :dayOfWeek")
    Optional<WorkSchedule> findByProfessionalUserIdAndDayOfWeek(@Param("userId") Long userId, @Param("dayOfWeek") DayOfWeek dayOfWeek);
}