package sesi.petvita.veterinary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sesi.petvita.user.model.UserModel;
import sesi.petvita.veterinary.model.VeterinaryModel;
import sesi.petvita.veterinary.speciality.SpecialityEnum;

import java.util.List;
import java.util.Optional;

@Repository
public interface VeterinaryRepository extends JpaRepository<VeterinaryModel, Long> {

    Optional<VeterinaryModel> findByUserAccount(UserModel userAccount);

    // --- BUSCAS PERSONALIZADAS (Corrigem o problema de busca por nome) ---

    // 1. Busca por Nome (no perfil Vet OU no User) E Especialidade
    @Query("SELECT v FROM VeterinaryModel v " +
            "LEFT JOIN v.userAccount u " +
            "WHERE (LOWER(v.name) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(u.username) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND v.specialityenum = :speciality")
    List<VeterinaryModel> findByNameAndSpeciality(@Param("name") String name, @Param("speciality") SpecialityEnum speciality);

    // 2. Busca Apenas por Nome (no perfil Vet OU no User)
    @Query("SELECT v FROM VeterinaryModel v " +
            "LEFT JOIN v.userAccount u " +
            "WHERE LOWER(v.name) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(u.username) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<VeterinaryModel> findByName(@Param("name") String name);

    // 3. Busca Apenas por Especialidade (Mantido o padrão do JPA)
    List<VeterinaryModel> findBySpecialityenum(SpecialityEnum speciality);

    // 4. Busca Todos com carregamento otimizado do usuário
    @Query("SELECT v FROM VeterinaryModel v LEFT JOIN FETCH v.userAccount")
    List<VeterinaryModel> findAllWithUser();
}