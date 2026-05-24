package com.tallyto.gestorfinanceiro.core.application.services;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/")
    public String home() {
        return "API ONLINE";
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}