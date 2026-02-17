package com.soaengry.moment.user.service;

import com.soaengry.moment.domain.email.repository.EmailVerificationRepository;
import com.soaengry.moment.domain.user.dto.request.SignupRequest;
import com.soaengry.moment.domain.user.dto.response.SignupResponse;
import com.soaengry.moment.domain.user.dto.response.UserResponse;
import com.soaengry.moment.domain.user.entity.User;
import com.soaengry.moment.domain.user.exception.UserErrorCode;
import com.soaengry.moment.domain.user.exception.UserException;
import com.soaengry.moment.domain.user.repository.RefreshTokenRepository;
import com.soaengry.moment.domain.user.repository.UserRepository;
import com.soaengry.moment.domain.user.service.AuthService;
import com.soaengry.moment.domain.user.service.UserService;
import com.soaengry.moment.global.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailVerificationRepository emailVerificationRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        emailVerificationRepository.deleteAll();
    }

    @Test
    @DisplayName("사용자 정보 조회 성공")
    void getUserInfo_Success() {
        // given
        SignupRequest signupRequest = new SignupRequest(
                "test@example.com",
                "Test1234!@",
                "테스터"
        );
        SignupResponse signupResponse = authService.signup(signupRequest);

        // when
        UserResponse response = userService.getUserInfo(signupResponse.userId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(signupResponse.userId());
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.nickname()).isEqualTo("테스터");
        assertThat(response.isEmailVerified()).isFalse();

        System.out.println("✅ 사용자 정보 조회 테스트 통과");
    }

    @Test
    @DisplayName("프로필 수정 성공")
    void updateProfile_Success() {
        // given
        SignupRequest signupRequest = new SignupRequest(
                "test@example.com",
                "Test1234!@",
                "테스터"
        );
        SignupResponse signupResponse = authService.signup(signupRequest);

        // when
        UserResponse response = userService.updateProfile(
                signupResponse.userId(),
                "새닉네임",
                "https://example.com/profile.jpg"
        );

        // then
        assertThat(response.nickname()).isEqualTo("새닉네임");
        assertThat(response.profileImageUrl()).isEqualTo("https://example.com/profile.jpg");

        System.out.println("✅ 프로필 수정 테스트 통과");
    }

    @Test
    @DisplayName("프로필 수정 실패 - 닉네임 중복")
    void updateProfile_Fail_DuplicateNickname() {
        // given
        SignupRequest signupRequest1 = new SignupRequest(
                "test1@example.com",
                "Test1234!@",
                "테스터1"
        );
        authService.signup(signupRequest1);

        SignupRequest signupRequest2 = new SignupRequest(
                "test2@example.com",
                "Test1234!@",
                "테스터2"
        );
        SignupResponse signupResponse2 = authService.signup(signupRequest2);

        // when & then
        assertThatThrownBy(() -> userService.updateProfile(
                signupResponse2.userId(),
                "테스터1",  // 이미 사용 중인 닉네임
                null
        ))
                .isInstanceOf(CustomException.class)
                .hasMessage(UserErrorCode.DUPLICATE_NICKNAME.getMessage());

        System.out.println("✅ 닉네임 중복 테스트 통과");
    }

    @Test
    @DisplayName("비밀번호 변경 성공")
    void changePassword_Success() {
        // given
        SignupRequest signupRequest = new SignupRequest(
                "test@example.com",
                "Test1234!@",
                "테스터"
        );
        SignupResponse signupResponse = authService.signup(signupRequest);

        // when
        userService.changePassword(
                signupResponse.userId(),
                "Test1234!@",
                "NewPassword123!@"
        );

        // then
        User user = userRepository.findById(signupResponse.userId()).orElseThrow();
        assertThat(passwordEncoder.matches("NewPassword123!@", user.getPassword())).isTrue();
        assertThat(user.getTokenVersion()).isEqualTo(1);  // 증가됨

        System.out.println("✅ 비밀번호 변경 테스트 통과");
    }

    @Test
    @DisplayName("비밀번호 변경 실패 - 현재 비밀번호 불일치")
    void changePassword_Fail_WrongCurrentPassword() {
        // given
        SignupRequest signupRequest = new SignupRequest(
                "test@example.com",
                "Test1234!@",
                "테스터"
        );
        SignupResponse signupResponse = authService.signup(signupRequest);

        // when & then
        assertThatThrownBy(() -> userService.changePassword(
                signupResponse.userId(),
                "WrongPassword123!@",
                "NewPassword123!@"
        ))
                .isInstanceOf(UserException.class)
                .hasMessage(UserErrorCode.AUTH_INVALID_CREDENTIALS.getMessage());

        System.out.println("✅ 잘못된 현재 비밀번호 테스트 통과");
    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    void deleteAccount_Success() {
        // given
        SignupRequest signupRequest = new SignupRequest(
                "test@example.com",
                "Test1234!@",
                "테스터"
        );
        SignupResponse signupResponse = authService.signup(signupRequest);

        // when
        userService.deleteAccount(signupResponse.userId());

        // then
        User user = userRepository.findById(signupResponse.userId()).orElseThrow();
        assertThat(user.isDeleted()).isTrue();
        assertThat(user.getDeletedAt()).isNotNull();

        System.out.println("✅ 회원 탈퇴 테스트 통과");
    }

    @Test
    @DisplayName("계정 복구 성공")
    void restoreAccount_Success() {
        // given
        SignupRequest signupRequest = new SignupRequest(
                "test@example.com",
                "Test1234!@",
                "테스터"
        );
        SignupResponse signupResponse = authService.signup(signupRequest);
        userService.deleteAccount(signupResponse.userId());

        // when
        userService.restoreAccount("test@example.com", "Test1234!@");

        // then
        User user = userRepository.findById(signupResponse.userId()).orElseThrow();
        assertThat(user.isDeleted()).isFalse();
        assertThat(user.getDeletedAt()).isNull();

        System.out.println("✅ 계정 복구 테스트 통과");
    }

    @Test
    @DisplayName("이메일 중복 체크 - 존재함")
    void checkEmailExists_True() {
        // given
        SignupRequest signupRequest = new SignupRequest(
                "test@example.com",
                "Test1234!@",
                "테스터"
        );
        authService.signup(signupRequest);

        // when
        boolean exists = userService.checkEmailExists("test@example.com");

        // then
        assertThat(exists).isTrue();

        System.out.println("✅ 이메일 중복 체크 (존재) 테스트 통과");
    }

    @Test
    @DisplayName("이메일 중복 체크 - 존재하지 않음")
    void checkEmailExists_False() {
        // when
        boolean exists = userService.checkEmailExists("nonexistent@example.com");

        // then
        assertThat(exists).isFalse();

        System.out.println("✅ 이메일 중복 체크 (미존재) 테스트 통과");
    }

    @Test
    @DisplayName("닉네임 중복 체크 - 존재함")
    void checkNicknameExists_True() {
        // given
        SignupRequest signupRequest = new SignupRequest(
                "test@example.com",
                "Test1234!@",
                "테스터"
        );
        authService.signup(signupRequest);

        // when
        boolean exists = userService.checkNicknameExists("테스터");

        // then
        assertThat(exists).isTrue();

        System.out.println("✅ 닉네임 중복 체크 (존재) 테스트 통과");
    }

    @Test
    @DisplayName("닉네임 중복 체크 - 존재하지 않음")
    void checkNicknameExists_False() {
        // when
        boolean exists = userService.checkNicknameExists("존재하지않는닉네임");

        // then
        assertThat(exists).isFalse();

        System.out.println("✅ 닉네임 중복 체크 (미존재) 테스트 통과");
    }
}
