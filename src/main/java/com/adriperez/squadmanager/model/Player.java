package com.adriperez.squadmanager.model;

import com.adriperez.squadmanager.model.enums.PlayerStatus;
import com.adriperez.squadmanager.model.enums.Position;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="players")
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String lastName;
    private Integer shirtNumber;
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Position position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerStatus playerStatus;

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MatchAppearance> matchAppearances = new ArrayList<>();

    @OneToMany(mappedBy = "player", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TrainingAttendance> trainingAttendances = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }

    private String injuryDescription;
    private LocalDate injuryStartDate;
    private LocalDate expectedReturnDate;

    public String getInjuryDescription() {
        return injuryDescription;
    }

    public void setInjuryDescription(String injuryDescription) {
        this.injuryDescription = injuryDescription;
    }

    public LocalDate getInjuryStartDate() {
        return injuryStartDate;
    }

    public void setInjuryStartDate(LocalDate injuryStartDate) {
        this.injuryStartDate = injuryStartDate;
    }

    public LocalDate getExpectedReturnDate() {
        return expectedReturnDate;
    }

    public void setExpectedReturnDate(LocalDate expectedReturnDate) {
        this.expectedReturnDate = expectedReturnDate;
    }

    public Player() {
    }

    public Player(String firstName, String lastName, int shirtNumber, LocalDate birthDate, Position position, PlayerStatus playerStatus) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.shirtNumber = shirtNumber;
        this.birthDate = birthDate;
        this.position = position;
        this.playerStatus = playerStatus;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getShirtNumber() {
        return shirtNumber;
    }

    public void setShirtNumber(int shirtNumber) {
        this.shirtNumber = shirtNumber;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public PlayerStatus getPlayerStatus() {
        return playerStatus;
    }

    public void setPlayerStatus(PlayerStatus playerStatus) {
        this.playerStatus = playerStatus;
    }

    public List<MatchAppearance> getMatchAppearances() {
        return matchAppearances;
    }

    public void setMatchAppearances(List<MatchAppearance> matchAppearances) {
        this.matchAppearances = matchAppearances;
    }

    public List<TrainingAttendance> getTrainingAttendances() {
        return trainingAttendances;
    }

    public void setTrainingAttendances(List<TrainingAttendance> trainingAttendances) {
        this.trainingAttendances = trainingAttendances;
    }
}
