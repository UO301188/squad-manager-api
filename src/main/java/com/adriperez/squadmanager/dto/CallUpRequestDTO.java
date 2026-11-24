package com.adriperez.squadmanager.dto;

import com.adriperez.squadmanager.model.enums.CallUpStatus;
import jakarta.validation.constraints.NotNull;

public class CallUpRequestDTO {

    @NotNull
    public Long playerId;

    @NotNull
    public CallUpStatus callUpStatus;


}
