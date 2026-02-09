package com.soaengry.moment.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * 이메일 인증 코드 발송
     */
    @Async
    public void sendVerificationEmail(String toEmail, String verificationCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("[MOMENT] 이메일 인증 코드");
            message.setText(String.format(
                    """
                            안녕하세요, MOMENT입니다.

                            이메일 인증 코드: %s

                            인증 코드는 5분간 유효합니다.
                            본인이 요청하지 않았다면 이 메일을 무시하세요.""",
                    verificationCode
            ));

            mailSender.send(message);
            log.info("이메일 인증 코드 발송 완료 - 수신: {}", toEmail);
        } catch (Exception e) {
            log.error("이메일 발송 실패 - 수신: {}, 오류: {}", toEmail, e.getMessage());
            // 실패해도 예외를 던지지 않음 (비동기이므로)
        }
    }

    /**
     * 비밀번호 재설정 링크 발송
     */
    @Async
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("[MOMENT] 비밀번호 재설정");
            message.setText(String.format(
                    """
                            안녕하세요, MOMENT입니다.

                            비밀번호 재설정 링크:
                            https://moment.com/reset-password?token=%s

                            링크는 1시간 동안 유효합니다.
                            본인이 요청하지 않았다면 이 메일을 무시하세요.""",
                    resetToken
            ));

            mailSender.send(message);
            log.info("비밀번호 재설정 메일 발송 완료 - 수신: {}", toEmail);
        } catch (Exception e) {
            log.error("비밀번호 재설정 메일 발송 실패 - 수신: {}, 오류: {}", toEmail, e.getMessage());
        }
    }

    /**
     * 환영 이메일 발송
     */
    @Async
    public void sendWelcomeEmail(String toEmail, String nickname) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("[MOMENT] 가입을 환영합니다!");
            message.setText(String.format(
                    """
                            안녕하세요, %s님!

                            MOMENT 가입을 진심으로 환영합니다.
                            특별한 순간을 함께 만들어가요.

                            감사합니다.
                            MOMENT 팀 드림""",
                    nickname
            ));

            mailSender.send(message);
            log.info("환영 메일 발송 완료 - 수신: {}", toEmail);
        } catch (Exception e) {
            log.error("환영 메일 발송 실패 - 수신: {}, 오류: {}", toEmail, e.getMessage());
        }
    }
}
