package com.adriperez.squadmanager.dto;

import com.adriperez.squadmanager.model.TrainingAttendance;
import com.adriperez.squadmanager.model.enums.AttendanceStatus;
import com.adriperez.squadmanager.model.enums.Position;

public record TrainingAttendanceResponseDTO(
        Long id,
        Long playerId,
        String playerFullName,
        Integer shirtNumber,
        Position position,
        AttendanceStatus attendanceStatus,
        String absenceReason,
        Integer rpe
) {
    public static TrainingAttendanceResponseDTO fromEntity(TrainingAttendance attendance) {
        return new TrainingAttendanceResponseDTO(
                attendance.getId(),
                attendance.getPlayer().getId(),
                attendance.getPlayer().getFirstName() + " " + attendance.getPlayer().getLastName(),
                attendance.getPlayer().getShirtNumber(),
                attendance.getPlayer().getPosition(),
                attendance.getAttendanceStatus(),
                attendance.getAbsenceReason(),
                attendance.getRpe()
        );
    }
}