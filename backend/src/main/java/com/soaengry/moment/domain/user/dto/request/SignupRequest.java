package com.soaengry.moment.domain.user.dto.request;


import com.soaengry.moment.domain.user.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "이메일 형식이 올바르지 않습니다")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()_+<>?,./-=]).{8,}$",
                message = "비밀번호는 8자 이상, 영문 대소문자, 숫자, 특수문자를 포함해야 합니다"
        )
        String password,

        @NotBlank(message = "닉네임은 필수입니다")
        @Size(min = 2, max = 50, message = "닉네임은 2-50자여야 합니다")
        String nickname
) {
    public User toEntity(String encodedPassword) {
        return User.builder()
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .role(User.Role.USER)
                .authProvider(User.AuthProvider.LOCAL)
                .isEmailVerified(false)
                .build();
    }
}