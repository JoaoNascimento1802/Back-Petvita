package sesi.petvita.employee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sesi.petvita.employee.model.EmployeeRating;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRatingRepository extends JpaRepository<EmployeeRating, Long> {
    List<EmployeeRating> findByEmployeeId(Long employeeId);
    Optional<EmployeeRating> findByEmployeeIdAndUserId(Long employeeId, Long userId);
}