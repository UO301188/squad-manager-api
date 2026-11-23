package com.adriperez.squadmanager.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="training_sessions")
public class TrainingSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime sessionDate;

    @Column(nullable = false)
    private String focus;

    private int durationMinutes;
    private String notes;

    @OneToMany(mappedBy = "trainingSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TrainingAttendance> trainingAttendances = new ArrayList<>();

    public TrainingSession() {
    }

    public TrainingSession(LocalDateTime sessionDate, String focus, int durationMinutes, String notes) {
        this.sessionDate = sessionDate;
        this.focus = focus;
        this.durationMinutes = durationMinutes;
        this.notes = notes;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getFocus() {
        return focus;
    }

    public void setFocus(String focus) {
        this.focus = focus;
    }

    public LocalDateTime getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDateTime sessionDate) {
        this.sessionDate = sessionDate;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<TrainingAttendance> getTrainingAttendances() {
        return trainingAttendances;
    }

    public void setTrainingAttendances(List<TrainingAttendance> trainingAttendances) {
        this.trainingAttendances = trainingAttendances;
    }
}
