package fr.iut.fitnesspark.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fr.iut.fitnesspark.model.Routines;

public interface RoutinesRepository extends JpaRepository<Routines, Long> {
    
    // Recherche par nom (méthode Spring Data)
    Page<Routines> findByNameContaining(String name, Pageable pageable);
    
    // Recherche JPQL alternative
    @Query("SELECT r FROM Routines r WHERE r.name LIKE :x OR r.description LIKE :x")
    Page<Routines> rechercher(@Param("x") String mc, Pageable pageable);
}
