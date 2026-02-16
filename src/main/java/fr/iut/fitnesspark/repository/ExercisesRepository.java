package fr.iut.fitnesspark.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import fr.iut.fitnesspark.model.Exercises;

public interface ExercisesRepository extends JpaRepository<Exercises, Long> {
    
    // Recherche par nom
    Page<Exercises> findByNameContaining(String name, Pageable pageable);
    
    // Recherche par routine
    Page<Exercises> findByRoutineId(Long routineId, Pageable pageable);
}
