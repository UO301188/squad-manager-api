package com.adriperez.squadmanager.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="matches")
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String opponent;
    @Column(nullable = false)
    private LocalDateTime matchDate;
    private String competition;
    private Boolean homeMatch;
    private Integer goalsFor;
    private Integer goalsAgainst;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MatchAppearance> matchAppearances = new ArrayList<>();

    public Match() {
    }

    public Match(String opponent, LocalDateTime matchDate, String competition, Boolean homeMatch, int goalsFor, int goalsAgainst) {
        this.opponent = opponent;
        this.matchDate = matchDate;
        this.competition = competition;
        this.homeMatch = homeMatch;
        this.goalsFor = goalsFor;
        this.goalsAgainst = goalsAgainst;
    }

    public Match(@NotBlank String opponent, @NotNull LocalDateTime matchDate, @NotBlank String competition, @NotNull Boolean homeMatch) {
        this.opponent = opponent;
        this.matchDate = matchDate;
        this.competition = competition;
        this.homeMatch = homeMatch;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getOpponent() {
        return opponent;
    }

    public void setOpponent(String opponent) {
        this.opponent = opponent;
    }

    public LocalDateTime getMatchDate() {
        return matchDate;
    }

    public void setMatchDate(LocalDateTime matchDate) {
        this.matchDate = matchDate;
    }

    public String getCompetition() {
        return competition;
    }

    public void setCompetition(String competition) {
        this.competition = competition;
    }

    public Boolean getHomeMatch() {
        return homeMatch;
    }

    public void setHomeMatch(Boolean homeMatch) {
        this.homeMatch = homeMatch;
    }

    public int getGoalsFor() {
        return goalsFor;
    }

    public void setGoalsFor(int goalsFor) {
        this.goalsFor = goalsFor;
    }

    public int getGoalsAgainst() {
        return goalsAgainst;
    }

    public void setGoalsAgainst(int goalsAgainst) {
        this.goalsAgainst = goalsAgainst;
    }

    public List<MatchAppearance> getMatchAppearances() {
        return matchAppearances;
    }

    public void setMatchAppearances(List<MatchAppearance> matchAppearances) {
        this.matchAppearances = matchAppearances;
    }
}
