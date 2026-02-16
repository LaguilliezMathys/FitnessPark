package fr.iut.fitnesspark.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.iut.fitnesspark.model.Routines;
import fr.iut.fitnesspark.repository.RoutinesRepository;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping("/api/routines")
public class RoutinesRestController {
    
    private final RoutinesRepository repoRoutines;
    
    public RoutinesRestController(RoutinesRepository repoRoutines) {
        this.repoRoutines = repoRoutines;
    }
    
    /**
     * GET /api/routines - Liste des routines (avec pagination optionnelle)
     * Paramètres optionnels : page, size, mc (mot-clé recherche)
     */
    @GetMapping
    public ResponseEntity<?> getAllRoutines(
        @RequestParam(value = "page", required = false) Integer page,
        @RequestParam(value = "size", required = false) Integer size,
        @RequestParam(value = "mc", required = false) String motCle
    ) {
        // Si pagination demandée
        if(page != null && size != null) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Routines> pageRoutines = (motCle != null && !motCle.isEmpty()) 
                ? repoRoutines.findByNameContaining(motCle, pageable)
                : repoRoutines.findAll(pageable);
            return ResponseEntity.ok(pageRoutines);
        }
        
        // Sinon, liste complète
        List<Routines> routines = (motCle != null && !motCle.isEmpty())
            ? repoRoutines.findByNameContaining(motCle, Pageable.unpaged()).getContent()
            : repoRoutines.findAll();
        return ResponseEntity.ok(routines);
    }
    
    /**
     * GET /api/routines/{id} - Détail d'une routine
     */
    @GetMapping("/{id}")
    public ResponseEntity<Routines> getRoutineById(@PathVariable Long id) {
        Optional<Routines> routine = repoRoutines.findById(id);
        return routine
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * POST /api/routines - Créer une nouvelle routine
     */
    @PostMapping
    public ResponseEntity<Routines> createRoutine(@Valid @RequestBody Routines routine) {
        routine.setId(null); // Force création
        Routines saved = repoRoutines.save(routine);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
    
    /**
     * DELETE /api/routines/{id} - Supprimer une routine
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoutine(@PathVariable Long id) {
        if(!repoRoutines.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repoRoutines.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
