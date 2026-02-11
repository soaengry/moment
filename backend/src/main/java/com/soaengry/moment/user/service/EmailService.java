package com.soaengry.moment.user.service;

import com.soaengry.moment.global.exception.BusinessException;
import com.soaengry.moment.global.exception.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.email.verification-url}")
    private String verificationUrl;

    @Value("${app.email.password-reset-url}")
    private String passwordResetUrl;

    /**
     * 이메일 인증 코드 발송
     */
    @Async
    public void sendVerificationEmail(String toEmail, String verificationCode) {
        try {
            String link = verificationUrl + "?token=" + verificationCode;
            String subject = "[MOMENT] 이메일 인증 코드";
            String content = buildVerificationEmailContent(link);

            sendEmail(toEmail, subject, content);
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
            String link = verificationUrl + "?token=" + resetToken;
            String subject = "[MOMENT] 이메일 인증 코드";
            String content = buildPasswordResetEmailContent(link);

            sendEmail(toEmail, subject, content);
            log.info("비밀번호 재설정 메일 발송 완료 - 수신: {}", toEmail);
        } catch (Exception e) {
            log.error("비밀번호 재설정 메일 발송 실패 - 수신: {}, 오류: {}", toEmail, e.getMessage());
        }
    }

    private void sendEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(message);
            log.info("이메일 전송 완료: {}", to);
        } catch (MessagingException e) {
            log.error("이메일 전송 실패: {}", to, e);
            throw new BusinessException(ErrorCode.EMAIL_001);
        }
    }

    private String buildVerificationEmailContent(String link) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                </head>
                <body>
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <h2>이메일 인증</h2>
                        <p>회원가입을 완료하려면 아래 버튼을 클릭해주세요.</p>
                        <p>링크는 24시간 동안 유효합니다.</p>
                        <div style="margin: 30px 0;">
                            <a href="%s"
                               style="background-color: #4CAF50; color: white; padding: 14px 20px; 
                                      text-decoration: none; border-radius: 4px; display: inline-block;">
                                이메일 인증하기
                            </a>
                        </div>
                        <p style="color: #666; font-size: 14px;">
                            버튼이 작동하지 않으면 아래 링크를 복사하여 브라우저에 붙여넣으세요:<br>
                            <a href="%s">%s</a>
                        </p>
                    </div>
                </body>
                </html>
                """.formatted(link, link, link);
    }

    private String buildPasswordResetEmailContent(String link) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                </head>
                <body>
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <h2>비밀번호 재설정</h2>
                        <p>비밀번호를 재설정하려면 아래 버튼을 클릭해주세요.</p>
                        <p>링크는 1시간 동안 유효합니다.</p>
                        <div style="margin: 30px 0;">
                            <a href="%s"
                               style="background-color: #2196F3; color: white; padding: 14px 20px; 
                                      text-decoration: none; border-radius: 4px; display: inline-block;">
                                비밀번호 재설정하기
                            </a>
                        </div>
                        <p style="color: #666; font-size: 14px;">
                            버튼이 작동하지 않으면 아래 링크를 복사하여 브라우저에 붙여넣으세요:<br>
                            <a href="%s">%s</a>
                        </p>
                        <p style="color: #f44336; font-size: 14px;">
                            본인이 요청하지 않은 경우 이 메일을 무시하세요.
                        </p>
                    </div>
                </body>
                </html>
                """.formatted(link, link, link);
    }
}