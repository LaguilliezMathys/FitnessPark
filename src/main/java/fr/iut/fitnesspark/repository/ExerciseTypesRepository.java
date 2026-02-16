package fr.iut.fitnesspark.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import fr.iut.fitnesspark.model.ExerciseTypes;

public interface ExerciseTypesRepository extends JpaRepository<ExerciseTypes, Long> {
    
    // Recherche par nom
    Page<ExerciseTypes> findByNameContaining(String name, Pageable pageable);
}
