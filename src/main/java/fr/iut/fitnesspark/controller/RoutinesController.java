package fr.iut.fitnesspark.controller;

import java.time.LocalDate;
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

import fr.iut.fitnesspark.model.Routines;
import fr.iut.fitnesspark.repository.RoutinesRepository;
import jakarta.validation.Valid;

@Controller
public class RoutinesController {
    
    private final RoutinesRepository repoRoutines;
    
    public RoutinesController(RoutinesRepository repoRoutines) {
        this.repoRoutines = repoRoutines;
    }
    
    @GetMapping("/routines")
    public String routines(
        @RequestParam(name = "mc", defaultValue = "") String motCle,
        @RequestParam(value = "p", defaultValue = "0") int page,
        @RequestParam(value = "s", defaultValue = "10") int size,
        @RequestParam(name = "act", defaultValue = "") String action,
        @RequestParam(name = "id", defaultValue = "0") Long id,
        Model model
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Routines> pageRoutines = motCle.isEmpty() 
            ? repoRoutines.findAll(pageable)
            : repoRoutines.findByNameContaining(motCle, pageable);
        
        model.addAttribute("routines", pageRoutines.getContent());
        model.addAttribute("page", pageRoutines);
        model.addAttribute("motCle", motCle);
        model.addAttribute("action", action);
        
        // Feedback après action (new/edit/del)
        if(!action.isEmpty() && id > 0) {
            repoRoutines.findById(id).ifPresent(r -> model.addAttribute("routine", r));
        }
        
        return "routines";
    }
    
    @GetMapping("/routineDetail")
    public String detail(
        @RequestParam("id") Long id,
        @RequestParam(name = "mc", defaultValue = "") String motCle,
        @RequestParam(value = "p", defaultValue = "0") int page,
        @RequestParam(value = "s", defaultValue = "10") int size,
        @RequestParam(name = "act", defaultValue = "") String action,
        Model model
    ) {
        Optional<Routines> routine = repoRoutines.findById(id);
        if(routine.isEmpty()) return "redirect:/routines";
        
        model.addAttribute("routine", routine.get());
        model.addAttribute("mc", motCle);
        model.addAttribute("p", page);
        model.addAttribute("s", size);
        model.addAttribute("action", action);
        
        return "routineDetail";
    }
    
    @GetMapping("/routineEdit")
    public String edit(
        @RequestParam(name = "id", defaultValue = "0") Long id,
        @RequestParam(name = "mc", defaultValue = "") String motCle,
        @RequestParam(value = "p", defaultValue = "0") int page,
        @RequestParam(value = "s", defaultValue = "10") int size,
        Model model
    ) {
        if(id > 0) {
            Optional<Routines> routine = repoRoutines.findById(id);
            if(routine.isEmpty()) return "redirect:/routines";
            model.addAttribute("routine", routine.get());
        } else {
            Routines newRoutine = new Routines();
            newRoutine.setCreationDate(LocalDate.now());
            newRoutine.setStatus(Routines.Status.active);
            model.addAttribute("routine", newRoutine);
        }
        model.addAttribute("p", page);
        model.addAttribute("s", size);
        model.addAttribute("mc", motCle);
        return "routineEdit";
    }
    
    @PostMapping("/routineSave")
    public String save(
        @Valid Routines routine,
        BindingResult bindingResult,
        @RequestParam(name = "mc", defaultValue = "") String motCle,
        @RequestParam(value = "p", defaultValue = "0") int page,
        @RequestParam(value = "s", defaultValue = "10") int size,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if(bindingResult.hasErrors()) {
            model.addAttribute("p", page);
            model.addAttribute("s", size);
            model.addAttribute("mc", motCle);
            return "routineEdit";
        }
        
        repoRoutines.save(routine);
        String act = (routine.getId() == null) ? "new" : "mod";
        
        redirectAttributes.addAttribute("act", act);
        redirectAttributes.addAttribute("id", routine.getId());
        redirectAttributes.addAttribute("p", page);
        redirectAttributes.addAttribute("s", size);
        redirectAttributes.addAttribute("mc", motCle);
        return "redirect:/routines";
    }
    
    @GetMapping("/routineDelete")
    public String delete(
        @RequestParam("id") Long id,
        @RequestParam(name = "mc", defaultValue = "") String motCle,
        @RequestParam(value = "p", defaultValue = "0") int page,
        @RequestParam(value = "s", defaultValue = "10") int size,
        RedirectAttributes redirectAttributes
    ) {
        repoRoutines.deleteById(id);
        redirectAttributes.addAttribute("act", "del");
        redirectAttributes.addAttribute("id", id);
        redirectAttributes.addAttribute("p", page);
        redirectAttributes.addAttribute("s", size);
        redirectAttributes.addAttribute("mc", motCle);
        return "redirect:/routines";
    }
}
