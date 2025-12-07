package sesi.petvita.veterinary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // Adicionei
import sesi.petvita.veterinary.model.VeterinaryRating;
import java.util.Optional;

@Repository
public interface VeterinaryRatingRepository extends JpaRepository<VeterinaryRating, Long> {

    // CORREÇÃO: Mudamos de 'findBy...' para 'findFirstBy...OrderByIdDesc'
    // Isso pega a avaliação mais recente (maior ID) e ignora as duplicatas antigas.
    Optional<VeterinaryRating> findFirstByVeterinaryIdAndUserIdOrderByIdDesc(Long veterinaryId, Long userId);
}