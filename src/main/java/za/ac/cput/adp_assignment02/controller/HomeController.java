package za.ac.cput.adp_assignment02.controller;

/*
HomeController.java
Home Controller class
Author: David Daniel Sepkitt (240046935)
Date: 10 August 2026
*/

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    @GetMapping("/")
    public String home() {
        return "ADP Assignment 02 Application is running";
    }
}
