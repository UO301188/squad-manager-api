package com.adriperez.squadmanager.dto;

import com.adriperez.squadmanager.model.enums.CallUpStatus;
import jakarta.validation.constraints.*;

public class MatchAppearanceStatsRequestDTO {

    @NotNull(message = "El estado de convocatoria es obligatorio")
    public CallUpStatus callUpStatus;

    @NotNull(message = "Los minutos jugados son obligatorios")
    @Min(value = 0, message = "Los minutos no pueden ser negativos")
    @Max(value = 120, message = "Los minutos no pueden superar 120")
    public Integer minutesPlayed;

    @NotNull(message = "Los goles son obligatorios")
    @Min(value = 0, message = "Los goles no pueden ser negativos")
    public Integer goals;

    @NotNull(message = "Las asistencias son obligatorias")
    @Min(value = 0, message = "Las asistencias no pueden ser negativas")
    public Integer assists;

    @NotNull(message = "Las tarjetas amarillas son obligatorias")
    @Min(value = 0, message = "Las tarjetas amarillas no pueden ser negativas")
    @Max(value = 2, message = "Máximo 2 tarjetas amarillas por partido")
    public Integer yellowCards;

    @NotNull(message = "El campo tarjeta roja es obligatorio")
    public Boolean redCard;

    @DecimalMin(value = "1.0", message = "La nota mínima es 1.0")
    @DecimalMax(value = "10.0", message = "La nota máxima es 10.0")
    public Double rating;
}