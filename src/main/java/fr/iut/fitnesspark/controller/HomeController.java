package fr.iut.fitnesspark.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;



@Controller
public class HomeController {
    
    @GetMapping("/")
    public String acceuil(){
        return "home";
    }

    @GetMapping("/home")
    public String home(){
        return "home";
    }


}