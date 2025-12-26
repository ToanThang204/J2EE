package com.hutech.demo.service;

import com.hutech.demo.model.Payment;
import com.hutech.demo.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class SepayService {

    @Value("${sepay.secret.key}")
    private String secretKey;

    @Value("${sepay.merchant.id}")
    private String merchantId;

    @Value("${sepay.qr.base.url}")
    private String qrBaseUrl;

    @Value("${sepay.bank.code}")
    private String bankCode;

    @Value("${sepay.account.number}")
    private String accountNumber;

    @Value("${sepay.account.name}")
    private String accountName;

    private final PaymentRepository paymentRepository;

    public SepayService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    /**
     * Tạo QR code thanh toán
     */
    public Map<String, Object> createQRPayment(Long userId, BigDecimal amount, String description) {
        try {
            String orderCode = "WEB_" + System.currentTimeMillis() + "_" + userId;
            
            // Generate QR URL
            String qrUrl = generateQRUrl(amount, description, orderCode);

            // Create payment record
            Payment payment = new Payment();
            payment.setUser(null); // Set user from userId if needed
            payment.setAmount(amount);
            payment.setCurrency("VND");
            payment.setStatus("pending");
            payment.setSepayOrderId(orderCode);
            payment.setSepayQrUrl(qrUrl);
            payment.setDescription(description);
            payment.setExpiredAt(LocalDateTime.now().plusMinutes(10));

            paymentRepository.save(payment);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("qr_url", qrUrl);
            result.put("order_code", orderCode);
            result.put("amount", amount);
            result.put("expired_at", payment.getExpiredAt());

            return result;

        } catch (Exception e) {
            log.error("Error creating QR payment", e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return result;
        }
    }

    /**
     * Generate QR URL
     */
    private String generateQRUrl(BigDecimal amount, String description, String orderCode) {
        try {
            String content = URLEncoder.encode(description + " - " + orderCode, StandardCharsets.UTF_8);
            return String.format("%s?acc=%s&bank=%s&amount=%s&des=%s",
                    qrBaseUrl, accountNumber, bankCode, amount.intValue(), content);
        } catch (Exception e) {
            log.error("Error generating QR URL", e);
            return null;
        }
    }

    /**
     * Verify webhook signature
     */
    public boolean verifyWebhook(Map<String, Object> data, String signature) {
        try {
            String payload = data.toString(); // Simplified, should use proper JSON serialization
            String expectedSignature = hmacSha256(payload, secretKey);
            return expectedSignature.equals(signature);
        } catch (Exception e) {
            log.error("Error verifying webhook signature", e);
            return false;
        }
    }

    /**
     * Generate HMAC SHA256 signature
     */
    private String hmacSha256(String data, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256Hmac.init(secretKeySpec);
        byte[] hash = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }
}
