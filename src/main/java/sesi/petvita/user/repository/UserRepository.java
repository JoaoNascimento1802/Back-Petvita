package sesi.petvita.user.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sesi.petvita.user.model.UserModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Long> {
    Optional<UserModel> findByEmail(String email);
    Optional<UserModel> findByUsername(String username);

    // Método atualizado para suportar paginação
    Page<UserModel> findByUsernameContainingIgnoreCase(String username, Pageable pageable);
}