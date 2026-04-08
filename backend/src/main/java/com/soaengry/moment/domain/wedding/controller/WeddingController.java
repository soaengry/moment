package com.soaengry.moment.domain.wedding.controller;

import com.soaengry.moment.domain.wedding.dto.request.AccountGroupRequest;
import com.soaengry.moment.domain.wedding.dto.request.AccountRequest;
import com.soaengry.moment.domain.wedding.dto.request.ScheduleRequest;
import com.soaengry.moment.domain.wedding.dto.request.WeddingRequest;
import com.soaengry.moment.domain.wedding.dto.response.AccountGroupResponse;
import com.soaengry.moment.domain.wedding.dto.response.AccountResponse;
import com.soaengry.moment.domain.wedding.dto.response.ScheduleResponse;
import com.soaengry.moment.domain.wedding.dto.response.WeddingResponse;
import com.soaengry.moment.domain.wedding.service.WeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/weddings")
@RequiredArgsConstructor
public class WeddingController {

    private final WeddingService weddingService;

    // ─── Wedding ───

    @PostMapping
    public ResponseEntity<WeddingResponse> createWedding(
            @RequestBody WeddingRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(weddingService.createWedding(userId, request));
    }

    @GetMapping("/{weddingId}")
    public ResponseEntity<WeddingResponse> getWedding(@PathVariable Long weddingId) {
        return ResponseEntity.ok(weddingService.getWedding(weddingId));
    }

    @PutMapping("/{weddingId}")
    public ResponseEntity<WeddingResponse> updateWedding(
            @PathVariable Long weddingId,
            @RequestBody WeddingRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(weddingService.updateWedding(weddingId, userId, request));
    }

    // ─── Schedule ───

    @PostMapping("/{weddingId}/schedules")
    public ResponseEntity<ScheduleResponse> createSchedule(
            @PathVariable Long weddingId,
            @RequestBody ScheduleRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(weddingService.createSchedule(weddingId, userId, request));
    }

    @GetMapping("/{weddingId}/schedules")
    public ResponseEntity<List<ScheduleResponse>> getSchedules(@PathVariable Long weddingId) {
        return ResponseEntity.ok(weddingService.getSchedules(weddingId));
    }

    @PutMapping("/schedules/{scheduleId}")
    public ResponseEntity<ScheduleResponse> updateSchedule(
            @PathVariable Long scheduleId,
            @RequestBody ScheduleRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(weddingService.updateSchedule(scheduleId, userId, request));
    }

    @DeleteMapping("/schedules/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(
            @PathVariable Long scheduleId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        weddingService.deleteSchedule(scheduleId, userId);
        return ResponseEntity.noContent().build();
    }

    // ─── AccountGroup ───

    @PostMapping("/{weddingId}/account-groups")
    public ResponseEntity<AccountGroupResponse> createAccountGroup(
            @PathVariable Long weddingId,
            @RequestBody AccountGroupRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(weddingService.createAccountGroup(weddingId, userId, request));
    }

    @GetMapping("/{weddingId}/account-groups")
    public ResponseEntity<List<AccountGroupResponse>> getAccountGroups(@PathVariable Long weddingId) {
        return ResponseEntity.ok(weddingService.getAccountGroups(weddingId));
    }

    @PutMapping("/account-groups/{groupId}")
    public ResponseEntity<AccountGroupResponse> updateAccountGroup(
            @PathVariable Long groupId,
            @RequestBody AccountGroupRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(weddingService.updateAccountGroup(groupId, userId, request));
    }

    @DeleteMapping("/account-groups/{groupId}")
    public ResponseEntity<Void> deleteAccountGroup(
            @PathVariable Long groupId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        weddingService.deleteAccountGroup(groupId, userId);
        return ResponseEntity.noContent().build();
    }

    // ─── Account ───

    @PostMapping("/account-groups/{groupId}/accounts")
    public ResponseEntity<AccountResponse> createAccount(
            @PathVariable Long groupId,
            @RequestBody AccountRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(weddingService.createAccount(groupId, userId, request));
    }

    @GetMapping("/account-groups/{groupId}/accounts")
    public ResponseEntity<List<AccountResponse>> getAccounts(@PathVariable Long groupId) {
        return ResponseEntity.ok(weddingService.getAccounts(groupId));
    }

    @PutMapping("/accounts/{accountId}")
    public ResponseEntity<AccountResponse> updateAccount(
            @PathVariable Long accountId,
            @RequestBody AccountRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(weddingService.updateAccount(accountId, userId, request));
    }

    @DeleteMapping("/accounts/{accountId}")
    public ResponseEntity<Void> deleteAccount(
            @PathVariable Long accountId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        weddingService.deleteAccount(accountId, userId);
        return ResponseEntity.noContent().build();
    }
}
