package com.soaengry.moment.user.service;

import com.soaengry.moment.domain.email.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class EmailServiceTest {

    @InjectMocks
    private EmailService emailService;

    @Mock
    private JavaMailSender mailSender;

    @Test
    @DisplayName("이메일 인증 코드 발송 성공")
    void sendVerificationEmail_Success() {
        // given
        String toEmail = "test@example.com";
        String verificationCode = "ABC123";

        // when
        emailService.sendVerificationEmail(toEmail, verificationCode);

        // then
        verify(mailSender, timeout(3000).times(1)).send(any(SimpleMailMessage.class));

        System.out.println("✅ 이메일 인증 코드 발송 테스트 통과");
    }

    @Test
    @DisplayName("비밀번호 재설정 이메일 발송 성공")
    void sendPasswordResetEmail_Success() {
        // given
        String toEmail = "test@example.com";
        String resetToken = "reset-token-123";

        // when
        emailService.sendPasswordResetEmail(toEmail, resetToken);

        // then
        verify(mailSender, timeout(3000).times(1)).send(any(SimpleMailMessage.class));

        System.out.println("✅ 비밀번호 재설정 이메일 발송 테스트 통과");
    }

    @Test
    @DisplayName("이메일 발송 실패 - 예외 발생해도 서비스는 정상 동작")
    void sendVerificationEmail_Fail_ShouldNotThrowException() {
        // given
        String toEmail = "test@example.com";
        String verificationCode = "ABC123";

        doThrow(new RuntimeException("Mail server error"))
                .when(mailSender).send(any(SimpleMailMessage.class));

        // when & then (예외가 발생하지 않아야 함)
        emailService.sendVerificationEmail(toEmail, verificationCode);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));

        System.out.println("✅ 이메일 발송 실패 처리 테스트 통과");
    }
}
