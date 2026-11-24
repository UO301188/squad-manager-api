package com.adriperez.squadmanager.controller;

import com.adriperez.squadmanager.dto.*;
import com.adriperez.squadmanager.service.TrainingSessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trainings")
public class TrainingSessionController {

    private final TrainingSessionService trainingSessionService;

    public TrainingSessionController(TrainingSessionService trainingSessionService) {
        this.trainingSessionService = trainingSessionService;
    }

    @PostMapping
    public ResponseEntity<TrainingSessionSummaryResponseDTO> createSession(
            @Valid @RequestBody TrainingSessionRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trainingSessionService.createSession(dto));
    }

    @GetMapping
    public ResponseEntity<List<TrainingSessionSummaryResponseDTO>> getAllSessions() {
        return ResponseEntity.ok(trainingSessionService.getAllSessions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrainingSessionDetailResponseDTO> getSessionById(@PathVariable Long id) {
        return ResponseEntity.ok(trainingSessionService.getSessionById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        trainingSessionService.deleteSession(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{sessionId}/attendances")
    public ResponseEntity<TrainingAttendanceResponseDTO> registerAttendance(
            @PathVariable Long sessionId,
            @Valid @RequestBody TrainingAttendanceRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(trainingSessionService.registerAttendance(sessionId, dto));
    }

    @PutMapping("/{sessionId}/attendances/{playerId}")
    public ResponseEntity<TrainingAttendanceResponseDTO> updateAttendance(
            @PathVariable Long sessionId,
            @PathVariable Long playerId,
            @Valid @RequestBody TrainingAttendanceRequestDTO dto) {
        return ResponseEntity.ok(trainingSessionService.updateAttendance(sessionId, playerId, dto));
    }
}