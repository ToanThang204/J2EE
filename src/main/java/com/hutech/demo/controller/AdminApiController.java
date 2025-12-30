package com.hutech.demo.controller;

import com.hutech.demo.dto.response.ApiResponse;
import com.hutech.demo.model.Payment;
import com.hutech.demo.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminApiController {

    private final PaymentRepository paymentRepository;
    private final com.hutech.demo.repository.CompanyRepository companyRepository;
    private final com.hutech.demo.service.SepayService sepayService;

    @GetMapping("/revenue-report")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRevenueReport(
            @RequestParam(defaultValue = "month") String period) {
        try {
            List<Payment> allPayments = paymentRepository.findAll();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startDate = getStartDate(period, now);

            List<Payment> filteredPayments = allPayments.stream()
                    .filter(p -> p.getCreatedAt() != null && p.getCreatedAt().isAfter(startDate))
                    .collect(Collectors.toList());

            BigDecimal totalRevenue = filteredPayments.stream()
                    .filter(p -> "paid".equals(p.getStatus()))
                    .map(p -> p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            long totalPayments = filteredPayments.size();
            long successfulPayments = filteredPayments.stream().filter(p -> "paid".equals(p.getStatus())).count();
            long pendingPayments = filteredPayments.stream().filter(p -> "pending".equals(p.getStatus())).count();

            // Map payments to simple summary to avoid serializing complex objects/proxies
            List<Map<String, Object>> paymentDocs = filteredPayments.stream().map(p -> {
                Map<String, Object> doc = new HashMap<>();
                doc.put("id", p.getId());
                doc.put("amount", p.getAmount());
                doc.put("status", p.getStatus());
                doc.put("paidAt", p.getPaidAt());
                doc.put("createdAt", p.getCreatedAt());

                if (p.getUser() != null) {
                    Map<String, Object> userDoc = new HashMap<>();
                    userDoc.put("name", p.getUser().getName());
                    doc.put("user", userDoc);
                }

                if (p.getCompany() != null) {
                    Map<String, Object> compDoc = new HashMap<>();
                    compDoc.put("companyName", p.getCompany().getCompanyName());
                    doc.put("company", compDoc);
                }

                return doc;
            }).collect(Collectors.toList());

            Map<String, Object> data = new HashMap<>();
            data.put("totalRevenue", totalRevenue);
            data.put("totalPayments", totalPayments);
            data.put("successfulPayments", successfulPayments);
            data.put("pendingPayments", pendingPayments);
            data.put("payments", paymentDocs);

            return ResponseEntity.ok(ApiResponse.success("Tải báo cáo doanh thu thành công", data));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi xử lý báo cáo: " + e.getMessage(), null));
        }
    }

    @PutMapping("/payments/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approvePayment(@PathVariable Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        sepayService.processSuccessfulPayment(payment);

        return ResponseEntity.ok(ApiResponse.success("Phê duyệt thanh toán thành công", null));
    }

    @PostMapping("/companies/{id}/request-payment")
    public ResponseEntity<ApiResponse<Map<String, Object>>> requestPayment(@PathVariable Long id,
            @RequestParam(defaultValue = "2000") BigDecimal amount) {
        com.hutech.demo.model.Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        com.hutech.demo.model.User user = company.getUser();
        if (user == null)
            throw new RuntimeException("Không tìm thấy người dùng đăng ký công ty này");

        String description = "Nang cap #" + company.getId();

        // Create QR using Sepay logic (but custom for this company)
        Map<String, Object> sepayResult = sepayService.createQRPayment(user.getId(), amount, description);

        if (!(boolean) sepayResult.get("success")) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Lỗi tạo QR: " + sepayResult.get("error"), null));
        }

        String orderCode = (String) sepayResult.get("order_code");

        // Find the payment created by sepayService and link it to company
        Payment payment = paymentRepository.findBySepayOrderId(orderCode)
                .orElseThrow(() -> new RuntimeException("Payment record not found after creation"));

        payment.setCompany(company);
        paymentRepository.save(payment);

        // Send Notification to User
        String paymentViewUrl = "/payment/order/" + orderCode;
        sepayService.sendNotification(user, "Yêu cầu thanh toán nâng cấp Nhà tuyển dụng",
                "Chào bạn, hồ sơ công ty " + company.getCompanyName()
                        + " đã được duyệt sơ bộ. Vui lòng thanh toán lệ phí " + amount
                        + " VND để hoàn tất kích hoạt.",
                paymentViewUrl);

        return ResponseEntity.ok(ApiResponse.success("Đã gửi yêu cầu thanh toán tới người dùng", sepayResult));
    }

    private LocalDateTime getStartDate(String period, LocalDateTime now) {
        switch (period) {
            case "today":
                return now.withHour(0).withMinute(0).withSecond(0).withNano(0);
            case "week":
                return now.minusWeeks(1);
            case "year":
                return now.withDayOfYear(1);
            case "month":
            default:
                return now.withDayOfMonth(1);
        }
    }
}
