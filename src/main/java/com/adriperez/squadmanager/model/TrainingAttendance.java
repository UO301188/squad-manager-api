package com.adriperez.squadmanager.model;

import com.adriperez.squadmanager.model.enums.AttendanceStatus;
import jakarta.persistence.*;

@Entity
@Table(name="training_attendance",
uniqueConstraints =  @UniqueConstraint(columnNames = {"player_id", "training_session_id"}))
public class TrainingAttendance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus attendanceStatus;

    private String absenceReason;
    private Integer rpe;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "training_session_id", nullable = false)
    private TrainingSession trainingSession;

    public TrainingAttendance() {
    }

    public TrainingAttendance(AttendanceStatus attendanceStatus, String absenceReason, Integer rpe, Player player, TrainingSession trainingSession) {
        this.attendanceStatus = attendanceStatus;
        this.absenceReason = absenceReason;
        this.rpe = rpe;
        this.player = player;
        this.trainingSession = trainingSession;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public AttendanceStatus getAttendanceStatus() {
        return attendanceStatus;
    }

    public void setAttendanceStatus(AttendanceStatus attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }

    public String getAbsenceReason() {
        return absenceReason;
    }

    public void setAbsenceReason(String absenceReason) {
        this.absenceReason = absenceReason;
    }

    public Integer getRpe() {
        return rpe;
    }

    public void setRpe(Integer rpe) {
        this.rpe = rpe;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public TrainingSession getTrainingSession() {
        return trainingSession;
    }

    public void setTrainingSession(TrainingSession trainingSession) {
        this.trainingSession = trainingSession;
    }
}
