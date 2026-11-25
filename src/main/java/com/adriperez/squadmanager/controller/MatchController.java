package com.adriperez.squadmanager.controller;

import com.adriperez.squadmanager.dto.*;
import com.adriperez.squadmanager.service.MatchService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/matches")
public class MatchController {

    private final MatchService matchService;

    public MatchController(MatchService matchService) {
        this.matchService = matchService;
    }

    @PostMapping
    public ResponseEntity<MatchSummaryResponseDTO> createMatch(@Valid @RequestBody MatchRequestDTO dto) {
        MatchSummaryResponseDTO createdMatch = matchService.createMatch(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMatch);
    }

    @GetMapping
    public ResponseEntity<List<MatchSummaryResponseDTO>> getAllMatches() {
        List<MatchSummaryResponseDTO> matches = matchService.getAllMatches();
        return ResponseEntity.ok(matches);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchDetailResponseDTO> getMatchById(@PathVariable Long id) {
        MatchDetailResponseDTO match = matchService.getMatchById(id);
        return ResponseEntity.ok(match);
    }

    @PatchMapping("/{id}/result")
    public ResponseEntity<MatchSummaryResponseDTO> updateMatchResult(
            @PathVariable Long id,
            @Valid @RequestBody MatchResultRequestDTO dto) {
        MatchSummaryResponseDTO updatedMatch = matchService.updateMatchResult(id, dto);
        return ResponseEntity.ok(updatedMatch);
    }

    @PostMapping("/{matchId}/appearances")
    public ResponseEntity<MatchAppearanceResponseDTO> addPlayerToMatch(
            @PathVariable Long matchId,
            @Valid @RequestBody CallUpRequestDTO dto) {
        MatchAppearanceResponseDTO appearance = matchService.addPlayerToMatch(matchId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(appearance);
    }

    @PutMapping("/{matchId}/appearances/{playerId}")
    public ResponseEntity<MatchAppearanceResponseDTO> updatePlayerAppearanceStats(
            @PathVariable Long matchId,
            @PathVariable Long playerId,
            @Valid @RequestBody MatchAppearanceStatsRequestDTO dto) {
        MatchAppearanceResponseDTO updatedAppearance = matchService.updatePlayerAppearanceStats(matchId, playerId, dto);
        return ResponseEntity.ok(updatedAppearance);
    }

    @DeleteMapping("/{matchId}/appearances/{playerId}")
    public ResponseEntity<Void> removePlayerFromMatch(
            @PathVariable Long matchId,
            @PathVariable Long playerId) {
        matchService.removePlayerFromMatch(matchId, playerId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/lineup")
    public ResponseEntity<MatchDetailResponseDTO> updateMatchLineup(
            @PathVariable Long id,
            @Valid @RequestBody LineupRequestDTO dto) {
        return ResponseEntity.ok(matchService.updateLineup(id, dto));
    }
}
