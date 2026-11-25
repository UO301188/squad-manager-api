package com.adriperez.squadmanager.model;

import com.adriperez.squadmanager.model.enums.MatchEventType;
import jakarta.persistence.*;

@Entity
@Table(name = "match_events")
public class MatchEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchEventType eventType;

    @Column(nullable = false)
    private Integer minute;

    private String notes;

    public MatchEvent() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Match getMatch() { return match; }
    public void setMatch(Match match) { this.match = match; }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }

    public MatchEventType getEventType() { return eventType; }
    public void setEventType(MatchEventType eventType) { this.eventType = eventType; }

    public Integer getMinute() { return minute; }
    public void setMinute(Integer minute) { this.minute = minute; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}