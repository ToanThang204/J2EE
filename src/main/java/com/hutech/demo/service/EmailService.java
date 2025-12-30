package com.hutech.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.hutech.demo.model.User;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Gửi email reset password
     */
    public void sendPasswordResetEmail(User user, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("Đặt lại mật khẩu - Hệ thống tuyển dụng");

            String resetUrl = "http://localhost:8081/auth/reset-password?token=" + token + "&email=" + user.getEmail();
            
            String htmlContent = "<html><body>" +
                "<h2>Xin chào " + user.getName() + ",</h2>" +
                "<p>Bạn đã yêu cầu đặt lại mật khẩu.</p>" +
                "<p>Vui lòng nhấp vào link bên dưới để đặt lại mật khẩu:</p>" +
                "<p><a href=\"" + resetUrl + "\">Đặt lại mật khẩu</a></p>" +
                "<p>Link này sẽ hết hạn sau 1 giờ.</p>" +
                "<p>Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.</p>" +
                "<br>" +
                "<p>Trân trọng,</p>" +
                "<p>Hệ thống tuyển dụng</p>" +
                "</body></html>";

            helper.setText(htmlContent, true);
            mailSender.send(message);

            log.info("Password reset email sent to: {}", user.getEmail());

        } catch (MessagingException e) {
            log.error("Error sending password reset email", e);
            throw new RuntimeException("Không thể gửi email");
        }
    }

    /**
     * Gửi email chào mừng
     */
    public void sendWelcomeEmail(User user) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("Chào mừng đến với Hệ thống tuyển dụng");

            String htmlContent = "<html><body>" +
                "<h2>Xin chào " + user.getName() + ",</h2>" +
                "<p>Chào mừng bạn đến với Hệ thống tuyển dụng!</p>" +
                "<p>Tài khoản của bạn đã được tạo thành công.</p>" +
                "<p>Bạn có thể đăng nhập và bắt đầu sử dụng hệ thống ngay bây giờ.</p>" +
                "<br>" +
                "<p>Trân trọng,</p>" +
                "<p>Hệ thống tuyển dụng</p>" +
                "</body></html>";

            helper.setText(htmlContent, true);
            mailSender.send(message);

            log.info("Welcome email sent to: {}", user.getEmail());

        } catch (MessagingException e) {
            log.error("Error sending welcome email", e);
        }
    }

    /**
     * Gửi email thông báo đơn ứng tuyển
     */
    public void sendApplicationSubmittedEmail(String toEmail, String jobTitle, String companyName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Đã nhận đơn ứng tuyển - " + jobTitle);

            String htmlContent = "<html><body>" +
                "<h2>Đơn ứng tuyển của bạn đã được gửi thành công!</h2>" +
                "<p><strong>Vị trí:</strong> " + jobTitle + "</p>" +
                "<p><strong>Công ty:</strong> " + companyName + "</p>" +
                "<p>Chúng tôi đã nhận được đơn ứng tuyển của bạn và sẽ xem xét trong thời gian sớm nhất.</p>" +
                "<br>" +
                "<p>Trân trọng,</p>" +
                "<p>" + companyName + "</p>" +
                "</body></html>";

            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            log.error("Error sending application submitted email", e);
        }
    }

    /**
     * Gửi email thông báo trạng thái đơn ứng tuyển
     */
    public void sendApplicationStatusEmail(String toEmail, String jobTitle, String status) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            
            String statusText = "đã cập nhật trạng thái";
            if ("reviewed".equalsIgnoreCase(status)) {
                statusText = "đã được xem xét";
            } else if ("interview".equalsIgnoreCase(status)) {
                statusText = "được mời phỏng vấn";
            } else if ("rejected".equalsIgnoreCase(status)) {
                statusText = "không đạt yêu cầu";
            } else if ("hired".equalsIgnoreCase(status)) {
                statusText = "đã được tuyển dụng";
            }

            helper.setSubject("Cập nhật đơn ứng tuyển - " + jobTitle);

            String htmlContent = "<html><body>" +
                "<h2>Thông báo cập nhật đơn ứng tuyển</h2>" +
                "<p><strong>Vị trí:</strong> " + jobTitle + "</p>" +
                "<p><strong>Trạng thái:</strong> " + statusText + "</p>" +
                "<p>Vui lòng đăng nhập vào hệ thống để xem chi tiết.</p>" +
                "<br>" +
                "<p>Trân trọng,</p>" +
                "<p>Hệ thống tuyển dụng</p>" +
                "</body></html>";

            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (MessagingException e) {
            log.error("Error sending application status email", e);
        }
    }
}
