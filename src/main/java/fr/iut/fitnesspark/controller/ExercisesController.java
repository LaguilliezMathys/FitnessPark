package fr.iut.fitnesspark.controller;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fr.iut.fitnesspark.model.Exercises;
import fr.iut.fitnesspark.repository.ExercisesRepository;
import fr.iut.fitnesspark.repository.RoutinesRepository;
import jakarta.validation.Valid;

@Controller
public class ExercisesController {
    
    private final ExercisesRepository repoExercises;
    private final RoutinesRepository repoRoutines;
    
    public ExercisesController(ExercisesRepository repoExercises, RoutinesRepository repoRoutines) {
        this.repoExercises = repoExercises;
        this.repoRoutines = repoRoutines;
    }
    
    @GetMapping("/exercises")
    public String exercises(
        @RequestParam(name = "mc", defaultValue = "") String motCle,
        @RequestParam(value = "p", defaultValue = "0") int page,
        @RequestParam(value = "s", defaultValue = "10") int size,
        @RequestParam(name = "act", defaultValue = "") String action,
        @RequestParam(name = "id", defaultValue = "0") Long id,
        Model model
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Exercises> pageExercises = motCle.isEmpty() 
            ? repoExercises.findAll(pageable)
            : repoExercises.findByNameContaining(motCle, pageable);
        
        model.addAttribute("exercises", pageExercises.getContent());
        model.addAttribute("page", pageExercises);
        model.addAttribute("motCle", motCle);
        model.addAttribute("action", action);
        
        // Feedback après action
        if(!action.isEmpty() && id > 0) {
            repoExercises.findById(id).ifPresent(e -> model.addAttribute("exercise", e));
        }
        
        return "exercises";
    }
    
    @GetMapping("/exerciseEdit")
    public String edit(
        @RequestParam(name = "id", defaultValue = "0") Long id,
        @RequestParam(name = "routineId", defaultValue = "0") Long routineId,
        @RequestParam(name = "returnUrl", defaultValue = "list") String returnUrl,
        @RequestParam(name = "mc", defaultValue = "") String motCle,
        @RequestParam(value = "p", defaultValue = "0") int page,
        @RequestParam(value = "s", defaultValue = "10") int size,
        Model model
    ) {
        if(id > 0) {
            Optional<Exercises> exercise = repoExercises.findById(id);
            if(exercise.isEmpty()) {
                return returnUrl.equals("detail") && routineId > 0
                    ? "redirect:/routineDetail?id=" + routineId
                    : "redirect:/exercises";
            }
            model.addAttribute("exercise", exercise.get());
        } else {
            Exercises newExercise = new Exercises();
            newExercise.setWeight(BigDecimal.ZERO);
            // Pré-sélectionner la routine si fournie
            if(routineId > 0) {
                repoRoutines.findById(routineId).ifPresent(newExercise::setRoutine);
            }
            model.addAttribute("exercise", newExercise);
        }
        model.addAttribute("routines", repoRoutines.findAll());
        model.addAttribute("routineId", routineId);
        model.addAttribute("returnUrl", returnUrl);
        model.addAttribute("p", page);
        model.addAttribute("s", size);
        model.addAttribute("mc", motCle);
        return "exerciseEdit";
    }
    
    @PostMapping("/exerciseSave")
    public String save(
        @Valid Exercises exercise,
        BindingResult bindingResult,
        @RequestParam(name = "routineId", defaultValue = "0") Long routineId,
        @RequestParam(name = "returnUrl", defaultValue = "list") String returnUrl,
        @RequestParam(name = "mc", defaultValue = "") String motCle,
        @RequestParam(value = "p", defaultValue = "0") int page,
        @RequestParam(value = "s", defaultValue = "10") int size,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if(bindingResult.hasErrors()) {
            model.addAttribute("routines", repoRoutines.findAll());
            model.addAttribute("routineId", routineId);
            model.addAttribute("returnUrl", returnUrl);
            model.addAttribute("p", page);
            model.addAttribute("s", size);
            model.addAttribute("mc", motCle);
            return "exerciseEdit";
        }
        
        repoExercises.save(exercise);
        String act = (exercise.getId() == null) ? "new" : "mod";
        
        redirectAttributes.addAttribute("act", act);
        redirectAttributes.addAttribute("id", exercise.getId());
        redirectAttributes.addAttribute("p", page);
        redirectAttributes.addAttribute("s", size);
        redirectAttributes.addAttribute("mc", motCle);
        
        // Retour selon le contexte
        if(returnUrl.equals("detail") && routineId > 0) {
            redirectAttributes.addAttribute("id", routineId);
            return "redirect:/routineDetail";
        }
        return "redirect:/exercises";
    }
    
    @GetMapping("/exerciseDelete")
    public String delete(
        @RequestParam("id") Long id,
        @RequestParam(name = "routineId", defaultValue = "0") Long routineId,
        @RequestParam(name = "returnUrl", defaultValue = "list") String returnUrl,
        @RequestParam(name = "mc", defaultValue = "") String motCle,
        @RequestParam(value = "p", defaultValue = "0") int page,
        @RequestParam(value = "s", defaultValue = "10") int size,
        RedirectAttributes redirectAttributes
    ) {
        repoExercises.deleteById(id);
        redirectAttributes.addAttribute("act", "del");
        redirectAttributes.addAttribute("p", page);
        redirectAttributes.addAttribute("s", size);
        redirectAttributes.addAttribute("mc", motCle);
        
        // Retour selon le contexte
        if(returnUrl.equals("detail") && routineId > 0) {
            redirectAttributes.addAttribute("id", routineId);
            return "redirect:/routineDetail";
        }
        return "redirect:/exercises";
    }
}
