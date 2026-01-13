package uth.edu.vn.lms_user_service.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Value("${spring.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from:noreply@lms.uth.edu.vn}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    public void sendPasswordResetEmail(String to, String token) {
        String resetLink = frontendUrl + "/reset-password?token=" + token;
        
        String subject = "🔐 Khôi phục mật khẩu - UTH LMS";
        String htmlContent = buildPasswordResetEmailTemplate(resetLink);

        sendHtmlEmail(to, subject, htmlContent);
    }

    @Async
    public void sendEmailVerification(String to, String token) {
        String verifyLink = frontendUrl + "/verify-email?token=" + token;
        
        String subject = "✅ Xác thực email - UTH LMS";
        String htmlContent = buildEmailVerificationTemplate(verifyLink);

        sendHtmlEmail(to, subject, htmlContent);
    }

    @Async
    public void sendWelcomeEmail(String to, String fullName) {
        String subject = "🎉 Chào mừng bạn đến với UTH LMS";
        String htmlContent = buildWelcomeEmailTemplate(fullName);

        sendHtmlEmail(to, subject, htmlContent);
    }

    @Async
    public void sendAccountLockedEmail(String to, String fullName, String reason) {
        String subject = "⚠️ Tài khoản bị khóa - UTH LMS";
        String htmlContent = buildAccountLockedEmailTemplate(fullName, reason);

        sendHtmlEmail(to, subject, htmlContent);
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        if (!mailEnabled) {
            System.out.println("=== EMAIL (Disabled) ===");
            System.out.println("To: " + to);
            System.out.println("Subject: " + subject);
            System.out.println("========================");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("✓ Email sent to: " + to);
        } catch (MessagingException e) {
            System.err.println("✗ Failed to send email to " + to + ": " + e.getMessage());
        }
    }

    private String buildPasswordResetEmailTemplate(String resetLink) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; background: #f5f5f5; margin: 0; padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 40px 30px; text-align: center; }
                    .header h1 { margin: 0; font-size: 28px; }
                    .content { padding: 40px 30px; }
                    .content p { color: #555; line-height: 1.8; font-size: 16px; }
                    .btn { display: inline-block; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white !important; padding: 16px 40px; text-decoration: none; border-radius: 30px; font-weight: bold; font-size: 16px; margin: 20px 0; }
                    .btn:hover { opacity: 0.9; }
                    .warning { background: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0; border-radius: 4px; }
                    .footer { background: #f8f9fa; padding: 20px 30px; text-align: center; color: #888; font-size: 14px; }
                    .link-text { word-break: break-all; background: #f0f0f0; padding: 10px; border-radius: 6px; font-size: 12px; margin-top: 10px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔐 Khôi Phục Mật Khẩu</h1>
                    </div>
                    <div class="content">
                        <p>Xin chào,</p>
                        <p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản UTH LMS của bạn.</p>
                        <p style="text-align: center;">
                            <a href="%s" class="btn">🔑 Đặt Lại Mật Khẩu</a>
                        </p>
                        <div class="warning">
                            <strong>⏰ Lưu ý:</strong> Link này sẽ hết hạn sau <strong>1 giờ</strong>. 
                            Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.
                        </div>
                        <p style="font-size: 14px; color: #888;">Nếu nút không hoạt động, hãy copy link bên dưới:</p>
                        <div class="link-text">%s</div>
                    </div>
                    <div class="footer">
                        <p>© 2026 UTH Learning Management System</p>
                        <p>Email này được gửi tự động, vui lòng không trả lời.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(resetLink, resetLink);
    }

    private String buildEmailVerificationTemplate(String verifyLink) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; background: #f5f5f5; margin: 0; padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #11998e 0%%, #38ef7d 100%%); color: white; padding: 40px 30px; text-align: center; }
                    .header h1 { margin: 0; font-size: 28px; }
                    .content { padding: 40px 30px; }
                    .content p { color: #555; line-height: 1.8; font-size: 16px; }
                    .btn { display: inline-block; background: linear-gradient(135deg, #11998e 0%%, #38ef7d 100%%); color: white !important; padding: 16px 40px; text-decoration: none; border-radius: 30px; font-weight: bold; font-size: 16px; margin: 20px 0; }
                    .footer { background: #f8f9fa; padding: 20px 30px; text-align: center; color: #888; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✅ Xác Thực Email</h1>
                    </div>
                    <div class="content">
                        <p>Xin chào,</p>
                        <p>Cảm ơn bạn đã đăng ký tài khoản UTH LMS. Vui lòng xác thực email của bạn bằng cách nhấn nút bên dưới:</p>
                        <p style="text-align: center;">
                            <a href="%s" class="btn">✓ Xác Thực Email</a>
                        </p>
                        <p style="font-size: 14px; color: #888;">Link xác thực sẽ hết hạn sau 24 giờ.</p>
                    </div>
                    <div class="footer">
                        <p>© 2026 UTH Learning Management System</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(verifyLink);
    }

    private String buildWelcomeEmailTemplate(String fullName) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; background: #f5f5f5; margin: 0; padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 40px 30px; text-align: center; }
                    .header h1 { margin: 0; font-size: 28px; }
                    .content { padding: 40px 30px; }
                    .content p { color: #555; line-height: 1.8; font-size: 16px; }
                    .footer { background: #f8f9fa; padding: 20px 30px; text-align: center; color: #888; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎉 Chào Mừng!</h1>
                    </div>
                    <div class="content">
                        <p>Xin chào <strong>%s</strong>,</p>
                        <p>Chào mừng bạn đến với UTH Learning Management System! Tài khoản của bạn đã được tạo thành công.</p>
                        <p>Hãy bắt đầu hành trình học tập của bạn ngay hôm nay!</p>
                    </div>
                    <div class="footer">
                        <p>© 2026 UTH Learning Management System</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(fullName);
    }

    private String buildAccountLockedEmailTemplate(String fullName, String reason) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Segoe UI', Arial, sans-serif; background: #f5f5f5; margin: 0; padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #ff416c 0%%, #ff4b2b 100%%); color: white; padding: 40px 30px; text-align: center; }
                    .header h1 { margin: 0; font-size: 28px; }
                    .content { padding: 40px 30px; }
                    .content p { color: #555; line-height: 1.8; font-size: 16px; }
                    .reason-box { background: #fee; border-left: 4px solid #ff4b2b; padding: 15px; margin: 20px 0; border-radius: 4px; }
                    .footer { background: #f8f9fa; padding: 20px 30px; text-align: center; color: #888; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>⚠️ Tài Khoản Bị Khóa</h1>
                    </div>
                    <div class="content">
                        <p>Xin chào <strong>%s</strong>,</p>
                        <p>Tài khoản UTH LMS của bạn đã bị khóa.</p>
                        <div class="reason-box">
                            <strong>Lý do:</strong> %s
                        </div>
                        <p>Nếu bạn cho rằng đây là sự nhầm lẫn, vui lòng liên hệ quản trị viên.</p>
                    </div>
                    <div class="footer">
                        <p>© 2026 UTH Learning Management System</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(fullName, reason != null ? reason : "Không xác định");
    }
}
