package com.adriperez.squadmanager.service;

import com.adriperez.squadmanager.dto.*;
import com.adriperez.squadmanager.exception.BusinessRuleException;
import com.adriperez.squadmanager.exception.ResourceNotFoundException;
import com.adriperez.squadmanager.model.AppUser;
import com.adriperez.squadmanager.model.Player;
import com.adriperez.squadmanager.model.TrainingAttendance;
import com.adriperez.squadmanager.model.TrainingSession;
import com.adriperez.squadmanager.model.enums.AttendanceStatus;
import com.adriperez.squadmanager.repository.PlayerRepository;
import com.adriperez.squadmanager.repository.TrainingAttendanceRepository;
import com.adriperez.squadmanager.repository.TrainingSessionRepository;
import com.adriperez.squadmanager.security.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TrainingSessionService {

    private final TrainingSessionRepository trainingSessionRepository;
    private final TrainingAttendanceRepository trainingAttendanceRepository;
    private final PlayerRepository playerRepository;
    private final CurrentUserService currentUserService;

    public TrainingSessionService(
            TrainingSessionRepository trainingSessionRepository,
            TrainingAttendanceRepository trainingAttendanceRepository,
            PlayerRepository playerRepository,
            CurrentUserService currentUserService) {
        this.trainingSessionRepository = trainingSessionRepository;
        this.trainingAttendanceRepository = trainingAttendanceRepository;
        this.playerRepository = playerRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public TrainingSessionSummaryResponseDTO createSession(TrainingSessionRequestDTO dto) {
        AppUser currentUser = currentUserService.getCurrentUser();

        TrainingSession session = new TrainingSession();
        session.setUser(currentUser);
        session.setSessionDate(dto.sessionDate());
        session.setFocus(dto.focus());
        session.setDurationMinutes(dto.durationMinutes());
        session.setNotes(dto.notes());

        TrainingSession saved = trainingSessionRepository.save(session);
        return TrainingSessionSummaryResponseDTO.fromEntity(saved);
    }

    public List<TrainingSessionSummaryResponseDTO> getAllSessions() {
        Long userId = currentUserService.getCurrentUser().getId();
        return trainingSessionRepository.findByUserIdOrderBySessionDateDesc(userId)
                .stream()
                .map(TrainingSessionSummaryResponseDTO::fromEntity)
                .toList();
    }

    public TrainingSessionDetailResponseDTO getSessionById(Long id) {
        Long userId = currentUserService.getCurrentUser().getId();
        TrainingSession session = trainingSessionRepository.findByIdAndUserIdWithAttendances(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Sesión de entrenamiento no encontrada con id: " + id));
        return TrainingSessionDetailResponseDTO.fromEntity(session);
    }

    @Transactional
    public void deleteSession(Long id) {
        Long userId = currentUserService.getCurrentUser().getId();
        TrainingSession session = trainingSessionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Sesión de entrenamiento no encontrada con id: " + id));
        trainingSessionRepository.delete(session);
    }

    @Transactional
    public TrainingAttendanceResponseDTO registerAttendance(Long sessionId, TrainingAttendanceRequestDTO dto) {
        Long userId = currentUserService.getCurrentUser().getId();

        TrainingSession session = trainingSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Sesión de entrenamiento no encontrada con id: " + sessionId));

        Player player = playerRepository.findByIdAndUserId(dto.playerId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado con id: " + dto.playerId()));

        if (trainingAttendanceRepository.existsByTrainingSessionIdAndPlayerId(sessionId, dto.playerId())) {
            throw new BusinessRuleException("La asistencia de este jugador ya está registrada en esta sesión");
        }

        validateAttendanceRules(dto);

        TrainingAttendance attendance = new TrainingAttendance();
        attendance.setTrainingSession(session);
        attendance.setPlayer(player);
        attendance.setAttendanceStatus(dto.attendanceStatus());
        attendance.setAbsenceReason(dto.absenceReason());
        attendance.setRpe(dto.rpe());

        TrainingAttendance saved = trainingAttendanceRepository.save(attendance);
        return TrainingAttendanceResponseDTO.fromEntity(saved);
    }

    @Transactional
    public TrainingAttendanceResponseDTO updateAttendance(Long sessionId, Long playerId, TrainingAttendanceRequestDTO dto) {
        Long userId = currentUserService.getCurrentUser().getId();

        if (trainingSessionRepository.findByIdAndUserId(sessionId, userId).isEmpty()) {
            throw new ResourceNotFoundException("Sesión de entrenamiento no encontrada con id: " + sessionId);
        }

        TrainingAttendance attendance = trainingAttendanceRepository.findByTrainingSessionIdAndPlayerId(sessionId, playerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe registro de asistencia para el jugador " + playerId + " en la sesión " + sessionId));

        validateAttendanceRules(dto);

        attendance.setAttendanceStatus(dto.attendanceStatus());
        attendance.setAbsenceReason(dto.absenceReason());
        attendance.setRpe(dto.rpe());

        TrainingAttendance updated = trainingAttendanceRepository.save(attendance);
        return TrainingAttendanceResponseDTO.fromEntity(updated);
    }

    private void validateAttendanceRules(TrainingAttendanceRequestDTO dto) {
        boolean isAbsent = dto.attendanceStatus() == AttendanceStatus.ABSENT_JUSTIFIED
                || dto.attendanceStatus() == AttendanceStatus.ABSENT_UNJUSTIFIED;

        if (isAbsent && dto.rpe() != null) {
            throw new BusinessRuleException("Un jugador ausente no puede registrar puntuación de esfuerzo (RPE)");
        }

        if (dto.attendanceStatus() == AttendanceStatus.ABSENT_JUSTIFIED
                && (dto.absenceReason() == null || dto.absenceReason().isBlank())) {
            throw new BusinessRuleException("Las ausencias justificadas requieren indicar un motivo");
        }
    }
}