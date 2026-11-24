package com.adriperez.squadmanager.model;

import com.adriperez.squadmanager.model.enums.CallUpStatus;
import jakarta.persistence.*;

@Entity
@Table(name="match_appearances",
        // Restricción de integridad: un jugador no puede aparecer dos veces en el mismo partido
        uniqueConstraints = @UniqueConstraint(columnNames = {"player_id", "match_id"})
)
public class MatchAppearance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CallUpStatus callUpStatus;

    private Integer goals;
    private Integer assists;
    private Integer yellowCards;
    private boolean redCard;
    private Integer minutesPlayed;

    public MatchAppearance() {
    }

    public MatchAppearance(Player player, CallUpStatus callUpStatus, Match match, int goals, int assists, int yellowCards, boolean redCard, int minutesPlayed) {
        this.player = player;
        this.callUpStatus = callUpStatus;
        this.match = match;
        this.goals = goals;
        this.assists = assists;
        this.yellowCards = yellowCards;
        this.redCard = redCard;
        this.minutesPlayed = minutesPlayed;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public CallUpStatus getCallUpStatus() {
        return callUpStatus;
    }

    public void setCallUpStatus(CallUpStatus callUpStatus) {
        this.callUpStatus = callUpStatus;
    }

    public Integer getGoals() {
        return goals;
    }

    public void setGoals(Integer goals) {
        this.goals = goals;
    }

    public Integer getAssists() {
        return assists;
    }

    public void setAssists(Integer assists) {
        this.assists = assists;
    }

    public Integer getYellowCards() {
        return yellowCards;
    }

    public void setYellowCards(Integer yellowCards) {
        this.yellowCards = yellowCards;
    }

    public boolean isRedCard() {
        return redCard;
    }

    public void setRedCard(boolean redCard) {
        this.redCard = redCard;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Match getMatch() {
        return match;
    }

    public void setMatch(Match match) {
        this.match = match;
    }

    public Integer getMinutesPlayed() {
        return minutesPlayed;
    }

    public void setMinutesPlayed(Integer minutesPlayed) {
        this.minutesPlayed = minutesPlayed;
    }
}
