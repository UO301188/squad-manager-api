package com.adriperez.squadmanager.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> checkHealth(){
        Map<String, String> response = Map.of("status", "OP");
        return ResponseEntity.ok(response);
    }
}