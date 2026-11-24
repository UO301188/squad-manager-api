package com.adriperez.squadmanager.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class MatchResultRequestDTO {

    @NotNull
    @Min(0)
    public Integer goalsFor;

    @NotNull
    @Min(0)
    public Integer goalsAgainst;

}
