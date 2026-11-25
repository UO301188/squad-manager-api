package com.adriperez.squadmanager.controller;

import com.adriperez.squadmanager.dto.*;
import com.adriperez.squadmanager.service.CallUpPdfService;
import com.adriperez.squadmanager.service.MatchService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/matches")
public class MatchController {

    private final MatchService matchService;
    private final CallUpPdfService callUpPdfService;

    public MatchController(MatchService matchService, CallUpPdfService callUpPdfService) {
        this.matchService = matchService;
        this.callUpPdfService = callUpPdfService;
    }

    @PostMapping
    public ResponseEntity<MatchSummaryResponseDTO> createMatch(@Valid @RequestBody MatchRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(matchService.createMatch(dto));
    }

    @GetMapping
    public ResponseEntity<List<MatchSummaryResponseDTO>> getAllMatches() {
        return ResponseEntity.ok(matchService.getAllMatches());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchDetailResponseDTO> getMatchById(@PathVariable Long id) {
        return ResponseEntity.ok(matchService.getMatchById(id));
    }

    @PatchMapping("/{id}/result")
    public ResponseEntity<MatchSummaryResponseDTO> updateMatchResult(
            @PathVariable Long id,
            @Valid @RequestBody MatchResultRequestDTO dto) {
        return ResponseEntity.ok(matchService.updateMatchResult(id, dto));
    }

    @PutMapping("/{id}/lineup")
    public ResponseEntity<MatchDetailResponseDTO> updateMatchLineup(
            @PathVariable Long id,
            @Valid @RequestBody LineupRequestDTO dto) {
        return ResponseEntity.ok(matchService.updateLineup(id, dto));
    }

    @PostMapping("/{matchId}/appearances")
    public ResponseEntity<MatchAppearanceResponseDTO> addPlayerToMatch(
            @PathVariable Long matchId,
            @Valid @RequestBody CallUpRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(matchService.addPlayerToMatch(matchId, dto));
    }

    @PutMapping("/{matchId}/appearances/{playerId}")
    public ResponseEntity<MatchAppearanceResponseDTO> updatePlayerAppearanceStats(
            @PathVariable Long matchId,
            @PathVariable Long playerId,
            @Valid @RequestBody MatchAppearanceStatsRequestDTO dto) {
        return ResponseEntity.ok(matchService.updatePlayerAppearanceStats(matchId, playerId, dto));
    }

    @DeleteMapping("/{matchId}/appearances/{playerId}")
    public ResponseEntity<Void> removePlayerFromMatch(
            @PathVariable Long matchId,
            @PathVariable Long playerId) {
        matchService.removePlayerFromMatch(matchId, playerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/callup-pdf")
    public ResponseEntity<byte[]> downloadCallUpPdf(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean includeRoles) {
        byte[] pdfBytes = callUpPdfService.generateCallUpPdf(id, includeRoles);
        String fileName = (includeRoles ? "convocatoria-alineacion-" : "convocatoria-previa-") + id + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @PostMapping("/{matchId}/events")
    public ResponseEntity<MatchEventResponseDTO> addMatchEvent(
            @PathVariable Long matchId,
            @Valid @RequestBody MatchEventRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(matchService.addMatchEvent(matchId, dto));
    }

    @DeleteMapping("/{matchId}/events/{eventId}")
    public ResponseEntity<Void> deleteMatchEvent(
            @PathVariable Long matchId,
            @PathVariable Long eventId) {
        matchService.deleteMatchEvent(matchId, eventId);
        return ResponseEntity.noContent().build();
    }
}