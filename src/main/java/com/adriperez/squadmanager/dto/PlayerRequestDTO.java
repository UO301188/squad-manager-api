package com.adriperez.squadmanager.dto;

import com.adriperez.squadmanager.model.enums.PlayerStatus;
import com.adriperez.squadmanager.model.enums.Position;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record PlayerRequestDTO(
        @NotBlank(message = "El nombre es obligatorio")
        String firstName,

        @NotBlank(message = "El apellido es obligatorio")
        String lastName,

        @NotNull(message = "El número de camiseta es obligatorio")
        @Min(value = 1, message = "El número de camiseta debe ser al menos 1")
        @Max(value = 99, message = "El número de camiseta no puede ser mayor a 99")
        Integer shirtNumber,

        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento debe ser en el pasado")
        LocalDate birthDate,

        @NotNull(message = "La posición es obligatoria")
        Position position,

        PlayerStatus playerStatus
) {

}
