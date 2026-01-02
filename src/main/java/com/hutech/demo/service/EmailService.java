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
            String statusColor = "#666";
            if ("REVIEWED".equalsIgnoreCase(status)) {
                statusText = "đã được xem xét";
                statusColor = "#2196F3";
            } else if ("INTERVIEW".equalsIgnoreCase(status)) {
                statusText = "được mời phỏng vấn";
                statusColor = "#FF9800";
            } else if ("REJECTED".equalsIgnoreCase(status)) {
                statusText = "không đạt yêu cầu";
                statusColor = "#F44336";
            } else if ("ACCEPTED".equalsIgnoreCase(status) || "HIRED".equalsIgnoreCase(status)) {
                statusText = "đã được tuyển dụng";
                statusColor = "#4CAF50";
            } else if ("PENDING".equalsIgnoreCase(status)) {
                statusText = "đang chờ xét duyệt";
                statusColor = "#9E9E9E";
            }

            helper.setSubject("Cập nhật đơn ứng tuyển - " + jobTitle);

            String htmlContent = "<!DOCTYPE html><html><head><style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }" +
                ".content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }" +
                ".status-badge { display: inline-block; padding: 10px 20px; background: " + statusColor + "; color: white; border-radius: 20px; font-weight: bold; margin: 10px 0; }" +
                ".info-box { background: white; padding: 20px; border-left: 4px solid " + statusColor + "; margin: 20px 0; border-radius: 5px; }" +
                ".btn { display: inline-block; padding: 12px 30px; background: #4CAF50; color: white; text-decoration: none; border-radius: 5px; margin-top: 20px; }" +
                ".footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }" +
                "</style></head><body>" +
                "<div class='container'>" +
                "<div class='header'><h1>📧 Cập nhật đơn ứng tuyển</h1></div>" +
                "<div class='content'>" +
                "<h2>Xin chào,</h2>" +
                "<p>Đơn ứng tuyển của bạn đã có cập nhật mới:</p>" +
                "<div class='info-box'>" +
                "<p><strong>📋 Vị trí:</strong> " + jobTitle + "</p>" +
                "<p><strong>📊 Trạng thái:</strong> <span class='status-badge'>" + statusText + "</span></p>" +
                "</div>" +
                "<p>Vui lòng đăng nhập vào hệ thống để xem chi tiết và cập nhật thêm thông tin.</p>" +
                "<a href='http://localhost:8081/candidate/applications' class='btn'>Xem chi tiết đơn ứng tuyển</a>" +
                "<div class='footer'>" +
                "<p>Email này được gửi tự động từ Hệ thống tuyển dụng WebCV</p>" +
                "<p>Nếu bạn có thắc mắc, vui lòng liên hệ với chúng tôi.</p>" +
                "</div></div></div>" +
                "</body></html>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            
            log.info("Application status email sent to: {} for job: {}", toEmail, jobTitle);

        } catch (MessagingException e) {
            log.error("Error sending application status email", e);
        }
    }

    /**
     * Gửi email thông báo nhà tuyển dụng có đơn ứng tuyển mới
     */
    public void sendNewApplicationReceivedEmail(String toEmail, String candidateName, String jobTitle) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("🎯 Đơn ứng tuyển mới - " + jobTitle);

            String htmlContent = "<!DOCTYPE html><html><head><style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }" +
                ".content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }" +
                ".highlight { background: white; padding: 20px; border-left: 4px solid #4CAF50; margin: 20px 0; border-radius: 5px; }" +
                ".btn { display: inline-block; padding: 12px 30px; background: #4CAF50; color: white; text-decoration: none; border-radius: 5px; margin-top: 20px; }" +
                ".footer { text-align: center; margin-top: 30px; color: #666; font-size: 12px; }" +
                "</style></head><body>" +
                "<div class='container'>" +
                "<div class='header'><h1>🎯 Đơn ứng tuyển mới</h1></div>" +
                "<div class='content'>" +
                "<h2>Xin chào,</h2>" +
                "<p>Bạn có một đơn ứng tuyển mới cho vị trí:</p>" +
                "<div class='highlight'>" +
                "<p><strong>👤 Ứng viên:</strong> " + candidateName + "</p>" +
                "<p><strong>📋 Vị trí:</strong> " + jobTitle + "</p>" +
                "<p><strong>📅 Thời gian:</strong> " + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "</p>" +
                "</div>" +
                "<p>Vui lòng đăng nhập vào hệ thống để xem xét hồ sơ ứng viên.</p>" +
                "<a href='http://localhost:8081/employer/applications' class='btn'>Xem đơn ứng tuyển</a>" +
                "<div class='footer'>" +
                "<p>Email này được gửi tự động từ Hệ thống tuyển dụng WebCV</p>" +
                "</div></div></div>" +
                "</body></html>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            
            log.info("New application notification sent to employer: {} for job: {}", toEmail, jobTitle);

        } catch (MessagingException e) {
            log.error("Error sending new application email to employer", e);
        }
    }
}
