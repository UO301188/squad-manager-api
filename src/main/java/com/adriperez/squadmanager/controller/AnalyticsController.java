package com.adriperez.squadmanager.controller;

import com.adriperez.squadmanager.dto.PlayerStatsResponseDTO;
import com.adriperez.squadmanager.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/players")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<PlayerStatsResponseDTO> getPlayerStats(@PathVariable Long id) {
        return ResponseEntity.ok(analyticsService.getPlayerStats(id));
    }

    @GetMapping("/stats")
    public ResponseEntity<List<PlayerStatsResponseDTO>> getAllPlayersStats() {
        return ResponseEntity.ok(analyticsService.getAllPlayersStats());
    }
}