package com.adriperez.squadmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class MatchRequestDTO {
    @NotBlank
    public String opponent;

    @NotNull
    public LocalDateTime matchDate;

    @NotBlank
    public String competition;

    @NotNull
    public Boolean homeMatch;
}
