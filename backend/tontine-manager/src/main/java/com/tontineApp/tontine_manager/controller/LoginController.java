package com.tontineApp.tontine_manager.controller;

import com.tontineApp.tontine_manager.dto.LoginRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class LoginController {

    @GetMapping("/user")
    public String getUser(){
        return "Bonjour , user!";
    }

    @GetMapping("/admin")
    public String getAdmin(){
        return "Bonjour , admin!";
    }

    @GetMapping("/")
    public String getHome(){
        return "Bonjour , home!";
    }


}
