package sesi.petvita.veterinary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sesi.petvita.veterinary.model.VeterinaryRating;

@Repository
public interface VeterinaryRatingRepository extends JpaRepository<VeterinaryRating, Long> {}