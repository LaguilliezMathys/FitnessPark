package fr.iut.fitnesspark.controller;

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

import fr.iut.fitnesspark.model.ExerciseTypes;
import fr.iut.fitnesspark.repository.ExerciseTypesRepository;
import jakarta.validation.Valid;

@Controller
public class ExerciseTypesController {
    
    private final ExerciseTypesRepository repoExerciseTypes;
    
    public ExerciseTypesController(ExerciseTypesRepository repoExerciseTypes) {
        this.repoExerciseTypes = repoExerciseTypes;
    }
    
    @GetMapping("/exerciseTypes")
    public String exerciseTypes(
        @RequestParam(name = "mc", defaultValue = "") String motCle,
        @RequestParam(value = "p", defaultValue = "0") int page,
        @RequestParam(value = "s", defaultValue = "20") int size,
        @RequestParam(name = "act", defaultValue = "") String action,
        @RequestParam(name = "id", defaultValue = "0") Long id,
        Model model
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ExerciseTypes> pageExerciseTypes = motCle.isEmpty() 
            ? repoExerciseTypes.findAll(pageable)
            : repoExerciseTypes.findByNameContaining(motCle, pageable);
        
        model.addAttribute("exerciseTypes", pageExerciseTypes.getContent());
        model.addAttribute("page", pageExerciseTypes);
        model.addAttribute("motCle", motCle);
        model.addAttribute("action", action);
        
        // Feedback après action
        if(!action.isEmpty() && id > 0) {
            repoExerciseTypes.findById(id).ifPresent(et -> model.addAttribute("exerciseType", et));
        }
        
        return "exerciseTypes";
    }
    
    @GetMapping("/exerciseTypeEdit")
    public String edit(
        @RequestParam(name = "id", defaultValue = "0") Long id,
        @RequestParam(name = "mc", defaultValue = "") String motCle,
        @RequestParam(value = "p", defaultValue = "0") int page,
        @RequestParam(value = "s", defaultValue = "20") int size,
        Model model
    ) {
        if(id > 0) {
            Optional<ExerciseTypes> exerciseType = repoExerciseTypes.findById(id);
            if(exerciseType.isEmpty()) return "redirect:/exerciseTypes";
            model.addAttribute("exerciseType", exerciseType.get());
        } else {
            model.addAttribute("exerciseType", new ExerciseTypes());
        }
        model.addAttribute("p", page);
        model.addAttribute("s", size);
        model.addAttribute("mc", motCle);
        return "exerciseTypeEdit";
    }
    
    @PostMapping("/exerciseTypeSave")
    public String save(
        @Valid ExerciseTypes exerciseType,
        BindingResult bindingResult,
        @RequestParam(name = "mc", defaultValue = "") String motCle,
        @RequestParam(value = "p", defaultValue = "0") int page,
        @RequestParam(value = "s", defaultValue = "20") int size,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if(bindingResult.hasErrors()) {
            model.addAttribute("p", page);
            model.addAttribute("s", size);
            model.addAttribute("mc", motCle);
            return "exerciseTypeEdit";
        }
        
        repoExerciseTypes.save(exerciseType);
        String act = (exerciseType.getId() == null) ? "new" : "mod";
        
        redirectAttributes.addAttribute("act", act);
        redirectAttributes.addAttribute("id", exerciseType.getId());
        redirectAttributes.addAttribute("p", page);
        redirectAttributes.addAttribute("s", size);
        redirectAttributes.addAttribute("mc", motCle);
        return "redirect:/exerciseTypes";
    }
    
    @GetMapping("/exerciseTypeDelete")
    public String delete(
        @RequestParam("id") Long id,
        @RequestParam(name = "mc", defaultValue = "") String motCle,
        @RequestParam(value = "p", defaultValue = "0") int page,
        @RequestParam(value = "s", defaultValue = "20") int size,
        RedirectAttributes redirectAttributes
    ) {
        repoExerciseTypes.deleteById(id);
        redirectAttributes.addAttribute("act", "del");
        redirectAttributes.addAttribute("id", id);
        redirectAttributes.addAttribute("p", page);
        redirectAttributes.addAttribute("s", size);
        redirectAttributes.addAttribute("mc", motCle);
        return "redirect:/exerciseTypes";
    }
}
