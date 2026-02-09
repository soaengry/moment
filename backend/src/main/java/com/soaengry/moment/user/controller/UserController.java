package com.soaengry.moment.user.controller;

import com.soaengry.moment.user.dto.UserResponse;
import com.soaengry.moment.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 내 정보 조회
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyInfo(@AuthenticationPrincipal Long userId) {
        UserResponse response = userService.getUserInfo(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 프로필 수정
     */
    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        UserResponse response = userService.updateProfile(
                userId, 
                request.nickname(), 
                request.profileImageUrl()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * 비밀번호 변경
     */
    @PatchMapping("/me/password")
    public ResponseEntity<ChangePasswordResponse> changePassword(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(userId, request.currentPassword(), request.newPassword());
        return ResponseEntity.ok(new ChangePasswordResponse("비밀번호가 변경되었습니다"));
    }

    /**
     * 회원 탈퇴
     */
    @DeleteMapping("/me")
    public ResponseEntity<DeleteAccountResponse> deleteAccount(
            @AuthenticationPrincipal Long userId
    ) {
        userService.deleteAccount(userId);
        return ResponseEntity.ok(new DeleteAccountResponse(
                "회원 탈퇴가 완료되었습니다. 30일 내 재가입 시 복구 가능합니다"
        ));
    }

    /**
     * 계정 복구
     */
    @PostMapping("/restore")
    public ResponseEntity<RestoreAccountResponse> restoreAccount(
            @Valid @RequestBody RestoreAccountRequest request
    ) {
        userService.restoreAccount(request.email(), request.password());
        return ResponseEntity.ok(new RestoreAccountResponse("계정이 복구되었습니다"));
    }
}

// Request DTOs
record UpdateProfileRequest(String nickname, String profileImageUrl) {}

record ChangePasswordRequest(
        @jakarta.validation.constraints.NotBlank String currentPassword,
        @jakarta.validation.constraints.NotBlank 
        @jakarta.validation.constraints.Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[~!@#$%^&*()_+<>?,./-=]).{8,}$",
                message = "비밀번호는 8자 이상, 영문 대소문자, 숫자, 특수문자를 포함해야 합니다"
        )
        String newPassword
) {}

record RestoreAccountRequest(
        @jakarta.validation.constraints.NotBlank 
        @jakarta.validation.constraints.Email 
        String email,
        @jakarta.validation.constraints.NotBlank String password
) {}

// Response DTOs
record ChangePasswordResponse(String message) {}
record DeleteAccountResponse(String message) {}
record RestoreAccountResponse(String message) {}
