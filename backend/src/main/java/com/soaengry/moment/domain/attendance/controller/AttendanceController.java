package com.soaengry.moment.domain.attendance.controller;

import com.soaengry.moment.domain.attendance.dto.request.AddAttendanceRequest;
import com.soaengry.moment.domain.attendance.dto.response.AttendanceResponse;
import com.soaengry.moment.domain.attendance.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/attendances")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping
    public ResponseEntity<List<AttendanceResponse>> getMyAttendances(
            @AuthenticationPrincipal Long userId) {
        List<AttendanceResponse> responses = attendanceService.getMyAttendances(userId);
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<AttendanceResponse> addAttendance(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody AddAttendanceRequest request) {
        AttendanceResponse response = attendanceService.addAttendance(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{attendanceId}")
    public ResponseEntity<Void> deleteAttendance(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long attendanceId) {
        attendanceService.deleteAttendance(userId, attendanceId);
        return ResponseEntity.noContent().build();
    }
}
