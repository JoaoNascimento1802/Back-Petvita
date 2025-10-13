package sesi.petvita.veterinary.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.veterinary.model.VeterinaryModel;
import sesi.petvita.veterinary.speciality.SpecialityEnum;

import java.util.List;
import java.util.Optional;


@Repository
public interface VeterinaryRepository extends JpaRepository<VeterinaryModel,Long> {
    Optional<VeterinaryModel> findByUserAccount(UserModel userAccount);

    List<VeterinaryModel> findByNameContainingIgnoreCase(String name);
    List<VeterinaryModel> findBySpecialityenum(SpecialityEnum speciality);
    List<VeterinaryModel> findByNameContainingIgnoreCaseAndSpecialityenum(String name, SpecialityEnum speciality);
}

