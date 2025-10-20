package sesi.petvita.employee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sesi.petvita.employee.model.TriageInfoModel;

@Repository
public interface TriageInfoRepository extends JpaRepository<TriageInfoModel, Long> {}