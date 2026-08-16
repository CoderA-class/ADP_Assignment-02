package za.ac.cput.adp_assignment02.controller;

/*
HomeController.java
Home Controller class
Author: David Daniel Sepkitt (240046935)
Date: 10 August 2026
*/

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;

@Controller
public class HomeController {
    @GetMapping("/")
    public String home() {
        return "forward:/index.html";
    }
}
