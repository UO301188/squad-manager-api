package com.adriperez.squadmanager.dto;

import com.adriperez.squadmanager.model.enums.CallUpStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class MatchAppearanceStatsRequestDTO {

    @NotNull
    public CallUpStatus callUpStatus;

    @NotNull @Min(0) @Max(120)
    public Integer minutesPlayed;

    @NotNull @Min(0)
    public Integer goals;

    @NotNull @Min(0)
    public Integer assists;

    @NotNull @Min(1) @Max(2)
    public Integer yellowCards;

    @NotNull
    public Boolean redCard;


}
