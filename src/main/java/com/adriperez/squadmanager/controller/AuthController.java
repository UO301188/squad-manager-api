package com.adriperez.squadmanager.controller;

import com.adriperez.squadmanager.dto.*;
import com.adriperez.squadmanager.exception.BusinessRuleException;
import com.adriperez.squadmanager.model.AppUser;
import com.adriperez.squadmanager.repository.AppUserRepository;
import com.adriperez.squadmanager.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthController(
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO dto) {
        String cleanEmail = dto.email().trim().toLowerCase();
        if (userRepository.existsByEmail(cleanEmail)) {
            throw new BusinessRuleException("Ya existe una cuenta registrada con ese email");
        }

        AppUser user = new AppUser(cleanEmail, passwordEncoder.encode(dto.password()), dto.clubName().trim());
        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponseDTO(token, user.getEmail(), user.getClubName()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.email().trim().toLowerCase(), dto.password())
            );
        } catch (BadCredentialsException e) {
            throw new BusinessRuleException("Email o contraseña incorrectos");
        }

        AppUser user = userRepository.findByEmail(dto.email().trim().toLowerCase()).orElseThrow();
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthResponseDTO(token, user.getEmail(), user.getClubName()));
    }
}