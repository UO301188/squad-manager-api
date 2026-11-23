package com.adriperez.squadmanager.controller;

import com.adriperez.squadmanager.dto.PlayerRequestDTO;
import com.adriperez.squadmanager.dto.PlayerResponseDTO;
import com.adriperez.squadmanager.model.enums.PlayerStatus;
import com.adriperez.squadmanager.model.enums.Position;
import com.adriperez.squadmanager.service.PlayerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/players")
public class PlayerController {
    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping
    public ResponseEntity<PlayerResponseDTO> create(@Valid @RequestBody PlayerRequestDTO dto) {
        PlayerResponseDTO createdPlayer = playerService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPlayer);
    }

    @GetMapping
    public ResponseEntity<List<PlayerResponseDTO>> findAll(
            @RequestParam(required = false) Position position,
            @RequestParam(required = false) PlayerStatus status
    ) {
        List<PlayerResponseDTO> players = playerService.findAll(position, status);
        return ResponseEntity.ok(players);
    }
    // 3. Obtener un jugador por ID -> 200 OK (o 404 si no existe)
    @GetMapping("/{id}")
    public ResponseEntity<PlayerResponseDTO> findById(@PathVariable Long id) {
        PlayerResponseDTO player = playerService.findById(id);
        return ResponseEntity.ok(player);
    }

    // 4. Actualizar un jugador existente -> 200 OK
    @PutMapping("/{id}")
    public ResponseEntity<PlayerResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody PlayerRequestDTO dto
    ) {
        PlayerResponseDTO updatedPlayer = playerService.update(id, dto);
        return ResponseEntity.ok(updatedPlayer);
    }

    // 5. Eliminar un jugador -> 204 No Content
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        playerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
