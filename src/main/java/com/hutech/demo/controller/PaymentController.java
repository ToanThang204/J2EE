package com.hutech.demo.controller;

import com.hutech.demo.dto.response.ApiResponse;
import com.hutech.demo.model.Payment;
import com.hutech.demo.repository.PaymentRepository;
import com.hutech.demo.service.SepayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final SepayService sepayService;
    private final PaymentRepository paymentRepository;

    @PostMapping("/create-qr")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createQRPayment(
            @RequestParam Long userId,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String description) {

        try {
            Map<String, Object> result = sepayService.createQRPayment(userId, amount, description);

            if ((boolean) result.get("success")) {
                return ResponseEntity.ok(ApiResponse.success("Tạo QR code thành công", result));
            } else {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error((String) result.get("error"), null));
            }
        } catch (Exception e) {
            log.error("Error creating QR payment", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Lỗi tạo QR code", null));
        }
    }

    @PostMapping({ "/webhook", "/hooks/sepay-payment" })
    public ResponseEntity<Map<String, String>> webhook(
            @RequestBody Map<String, Object> data,
            @RequestHeader(value = "X-Sepay-Signature", required = false) String signature) {

        log.info("Received Sepay webhook: {}", data);

        try {
            // Verify signature
            if (signature == null || !sepayService.verifyWebhook(data, signature)) {
                log.warn("Invalid webhook signature");
                return ResponseEntity.status(401).body(Map.of("error", "Invalid signature"));
            }

            String orderId = (String) data.getOrDefault("order_id", data.get("order_code"));
            String status = (String) data.getOrDefault("status", data.get("payment_status"));

            if (orderId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Missing order_id"));
            }

            // Find payment
            Payment payment = paymentRepository.findBySepayOrderId(orderId).orElse(null);
            if (payment == null) {
                log.warn("Payment not found for order: {}", orderId);
                return ResponseEntity.status(404).body(Map.of("error", "Payment not found"));
            }

            // Update payment status using unified logic
            if ("paid".equals(status) || "success".equals(status)) {
                sepayService.processSuccessfulPayment(payment);
                log.info("Payment processed successfully via webhook: {}", orderId);
            }

            return ResponseEntity.ok(Map.of("message", "Webhook processed"));

        } catch (Exception e) {
            log.error("Error processing webhook", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/check-status/{orderId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkStatus(@PathVariable String orderId) {
        Payment payment = paymentRepository.findBySepayOrderId(orderId).orElse(null);

        if (payment == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> data = Map.of(
                "id", payment.getId(),
                "status", payment.getStatus(),
                "amount", payment.getAmount(),
                "orderCode", payment.getSepayOrderId());

        return ResponseEntity.ok(ApiResponse.success("Payment found", data));
    }
}
