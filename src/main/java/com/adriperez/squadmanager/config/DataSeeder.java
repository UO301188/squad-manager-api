package com.adriperez.squadmanager.config;

import com.adriperez.squadmanager.model.*;
import com.adriperez.squadmanager.model.enums.*;
import com.adriperez.squadmanager.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;
    private final MatchAppearanceRepository matchAppearanceRepository;
    private final TrainingSessionRepository trainingSessionRepository;
    private final TrainingAttendanceRepository trainingAttendanceRepository;

    public DataSeeder(
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            PlayerRepository playerRepository,
            MatchRepository matchRepository,
            MatchAppearanceRepository matchAppearanceRepository,
            TrainingSessionRepository trainingSessionRepository,
            TrainingAttendanceRepository trainingAttendanceRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.playerRepository = playerRepository;
        this.matchRepository = matchRepository;
        this.matchAppearanceRepository = matchAppearanceRepository;
        this.trainingSessionRepository = trainingSessionRepository;
        this.trainingAttendanceRepository = trainingAttendanceRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByEmail("demo@squadmanager.com")) {
            return;
        }

        AppUser demoUser = userRepository.save(
                new AppUser("demo@squadmanager.com", passwordEncoder.encode("demo1234"), "Real Oviedo Demo FC")
        );

        Player p1 = createPlayer(demoUser, "Aarón", "Escandell", 13, LocalDate.of(1995, 9, 27), Position.GOALKEEPER, PlayerStatus.AVAILABLE);
        Player p2 = createPlayer(demoUser, "David", "Costas", 4, LocalDate.of(1995, 3, 26), Position.DEFENDER, PlayerStatus.AVAILABLE);
        Player p3 = createPlayer(demoUser, "Dani", "Calvo", 12, LocalDate.of(1994, 4, 1), Position.DEFENDER, PlayerStatus.AVAILABLE);
        Player p4 = createPlayer(demoUser, "Oier", "Luengo", 15, LocalDate.of(1997, 11, 11), Position.DEFENDER, PlayerStatus.AVAILABLE);
        Player p5 = createPlayer(demoUser, "Santi", "Cazorla", 8, LocalDate.of(1984, 12, 13), Position.MIDFIELDER, PlayerStatus.AVAILABLE);
        Player p6 = createPlayer(demoUser, "Santiago", "Colombatto", 11, LocalDate.of(1997, 1, 17), Position.MIDFIELDER, PlayerStatus.AVAILABLE);
        Player p7 = createPlayer(demoUser, "Kwasi", "Sibo", 6, LocalDate.of(1998, 6, 24), Position.MIDFIELDER, PlayerStatus.SUSPENDED);
        Player p8 = createPlayer(demoUser, "Ilyas", "Chaira", 7, LocalDate.of(2001, 2, 2), Position.FORWARD, PlayerStatus.AVAILABLE);
        Player p9 = createPlayer(demoUser, "Haissem", "Hassan", 10, LocalDate.of(2002, 2, 8), Position.FORWARD, PlayerStatus.AVAILABLE);
        Player p10 = createPlayer(demoUser, "Alemão", "Zurawski", 9, LocalDate.of(1998, 4, 1), Position.FORWARD, PlayerStatus.INJURED);

        List<Player> players = playerRepository.saveAll(List.of(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10));

        Match playedMatch = new Match();
        playedMatch.setUser(demoUser);
        playedMatch.setOpponent("Real Sporting");
        playedMatch.setMatchDate(LocalDateTime.now().minusDays(4).withHour(18).withMinute(30));
        playedMatch.setCompetition("Liga Hypermotion");
        playedMatch.setHomeMatch(true);
        playedMatch.setGoalsFor(2);
        playedMatch.setGoalsAgainst(1);
        matchRepository.save(playedMatch);

        Match nextMatch = new Match();
        nextMatch.setUser(demoUser);
        nextMatch.setOpponent("Racing de Santander");
        nextMatch.setMatchDate(LocalDateTime.now().plusDays(3).withHour(21).withMinute(0));
        nextMatch.setCompetition("Liga Hypermotion");
        nextMatch.setHomeMatch(false);
        matchRepository.save(nextMatch);

        matchAppearanceRepository.saveAll(List.of(
                createAppearance(playedMatch, players.get(0), CallUpStatus.STARTER, 90, 0, 0, 0, false),
                createAppearance(playedMatch, players.get(1), CallUpStatus.STARTER, 90, 0, 0, 1, false),
                createAppearance(playedMatch, players.get(2), CallUpStatus.STARTER, 90, 1, 0, 0, false),
                createAppearance(playedMatch, players.get(4), CallUpStatus.STARTER, 78, 0, 2, 0, false),
                createAppearance(playedMatch, players.get(5), CallUpStatus.STARTER, 90, 0, 0, 1, false),
                createAppearance(playedMatch, players.get(7), CallUpStatus.STARTER, 82, 1, 0, 0, false),
                createAppearance(playedMatch, players.get(8), CallUpStatus.SUBSTITUTE, 24, 0, 0, 0, false)
        ));

        TrainingSession s1 = new TrainingSession();
        s1.setUser(demoUser);
        s1.setSessionDate(LocalDateTime.now().minusDays(2).withHour(10).withMinute(30));
        s1.setFocus("Presión tras pérdida y salida de balón");
        s1.setDurationMinutes(95);
        s1.setNotes("Alta intensidad en espacios reducidos.");
        trainingSessionRepository.save(s1);

        trainingAttendanceRepository.saveAll(List.of(
                createAttendance(s1, players.get(0), AttendanceStatus.PRESENT, null, 6),
                createAttendance(s1, players.get(1), AttendanceStatus.PRESENT, null, 8),
                createAttendance(s1, players.get(4), AttendanceStatus.PRESENT, null, 7),
                createAttendance(s1, players.get(7), AttendanceStatus.PRESENT, null, 9),
                createAttendance(s1, players.get(9), AttendanceStatus.INJURED_RECOVERY, "Readaptación en gimnasio", 4)
        ));
    }

    private Player createPlayer(AppUser user, String first, String last, int dorsal, LocalDate birth, Position pos, PlayerStatus status) {
        Player p = new Player();
        p.setUser(user);
        p.setFirstName(first);
        p.setLastName(last);
        p.setShirtNumber(dorsal);
        p.setBirthDate(birth);
        p.setPosition(pos);
        p.setPlayerStatus(status);
        return p;
    }

    private MatchAppearance createAppearance(Match m, Player p, CallUpStatus status, int min, int goals, int assists, int yellow, boolean red) {
        MatchAppearance ma = new MatchAppearance();
        ma.setMatch(m);
        ma.setPlayer(p);
        ma.setCallUpStatus(status);
        ma.setMinutesPlayed(min);
        ma.setGoals(goals);
        ma.setAssists(assists);
        ma.setYellowCards(yellow);
        ma.setRedCard(red);
        return ma;
    }

    private TrainingAttendance createAttendance(TrainingSession s, Player p, AttendanceStatus status, String reason, Integer rpe) {
        TrainingAttendance ta = new TrainingAttendance();
        ta.setTrainingSession(s);
        ta.setPlayer(p);
        ta.setAttendanceStatus(status);
        ta.setAbsenceReason(reason);
        ta.setRpe(rpe);
        return ta;
    }
}