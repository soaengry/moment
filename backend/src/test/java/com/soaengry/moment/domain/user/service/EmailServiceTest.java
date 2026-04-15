package com.soaengry.moment.domain.user.service;

import com.soaengry.moment.domain.email.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    @MockitoBean
    private JavaMailSender mailSender;

    @Test
    @DisplayName("이메일 인증 코드 발송 성공")
    void sendVerificationEmail_Success() {
        // given
        String toEmail = "test@example.com";
        String verificationCode = "ABC123";
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // when
        emailService.sendVerificationEmail(toEmail, verificationCode);

        // then
        verify(mailSender, timeout(3000).times(1)).send(any(MimeMessage.class));

        System.out.println("✅ 이메일 인증 코드 발송 테스트 통과");
    }

    @Test
    @DisplayName("비밀번호 재설정 이메일 발송 성공")
    void sendPasswordResetEmail_Success() {
        // given
        String toEmail = "test@example.com";
        String resetToken = "reset-token-123";
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // when
        emailService.sendPasswordResetEmail(toEmail, resetToken);

        // then
        verify(mailSender, timeout(3000).times(1)).send(any(MimeMessage.class));

        System.out.println("✅ 비밀번호 재설정 이메일 발송 테스트 통과");
    }

    @Test
    @DisplayName("이메일 발송 실패 - 예외 발생해도 서비스는 정상 동작")
    void sendVerificationEmail_Fail_ShouldNotThrowException() {
        // given
        String toEmail = "test@example.com";
        String verificationCode = "ABC123";
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("Mail server error"))
                .when(mailSender).send(any(MimeMessage.class));

        // when & then (예외가 발생하지 않아야 함)
        emailService.sendVerificationEmail(toEmail, verificationCode);

        verify(mailSender, times(1)).send(any(MimeMessage.class));

        System.out.println("✅ 이메일 발송 실패 처리 테스트 통과");
    }
}
