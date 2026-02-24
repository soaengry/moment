package com.soaengry.moment.domain.user.controller;

import com.soaengry.moment.domain.user.dto.request.ChangePasswordRequest;
import com.soaengry.moment.domain.user.dto.request.RestoreAccountRequest;
import com.soaengry.moment.domain.user.dto.response.ChangePasswordResponse;
import com.soaengry.moment.domain.user.dto.response.DeleteAccountResponse;
import com.soaengry.moment.domain.user.dto.response.RestoreAccountResponse;
import com.soaengry.moment.domain.user.dto.response.UserResponse;
import com.soaengry.moment.domain.user.service.UserService;
import com.soaengry.moment.global.service.S3Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final S3Service s3Service;

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
    @PatchMapping(value = "/me", consumes = {"application/json", "multipart/form-data"})
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal Long userId,
            @RequestPart(value = "nickname", required = false) String nickname,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestPart(value = "removeProfileImage", required = false) String removeProfileImage
    ) {
        // 현재 사용자 정보 조회
        UserResponse currentUser = userService.getUserInfo(userId);
        String currentImageUrl = currentUser.profileImageUrl();

        String newProfileImageUrl = currentImageUrl;
        boolean shouldRemoveImage = "true".equalsIgnoreCase(removeProfileImage);

        // 1. 프로필 이미지 삭제 요청
        if (shouldRemoveImage) {
            if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
                s3Service.deleteFile(currentImageUrl);
                log.info("기존 프로필 이미지 삭제 - URL: {}", currentImageUrl);
            }
            newProfileImageUrl = null;
        }
        // 2. 새 프로필 이미지 업로드
        else if (profileImage != null && !profileImage.isEmpty()) {
            // 기존 이미지가 있으면 S3에서 삭제
            if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
                s3Service.deleteFile(currentImageUrl);
                log.info("기존 프로필 이미지 삭제 - URL: {}", currentImageUrl);
            }

            // 새 이미지 업로드
            newProfileImageUrl = s3Service.uploadProfileImage(profileImage);
            log.info("새 프로필 이미지 업로드 완료 - URL: {}", newProfileImageUrl);
        }

        UserResponse response = userService.updateProfile(userId, nickname, newProfileImageUrl);
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