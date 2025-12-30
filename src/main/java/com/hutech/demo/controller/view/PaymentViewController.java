package com.hutech.demo.controller.view;

import com.hutech.demo.model.Payment;
import com.hutech.demo.repository.PaymentRepository;
import com.hutech.demo.service.SepayService;
import com.hutech.demo.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class PaymentViewController {

    private final PaymentRepository paymentRepository;
    private final SepayService sepayService;

    @GetMapping("/payment/{id}")
    public String showPayment(@PathVariable Long id, Model model) {
        Payment payment = paymentRepository.findById(id).orElse(null);

        model.addAttribute("payment", payment);
        model.addAttribute("bankCode", sepayService.getBankCode());
        model.addAttribute("accountNumber", sepayService.getAccountNumber());
        model.addAttribute("accountName", sepayService.getAccountName());
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());

        return "payment/show";
    }

    @GetMapping("/payment/order/{orderCode}")
    public String showPaymentByOrder(@PathVariable String orderCode, Model model) {
        Payment payment = paymentRepository.findBySepayOrderId(orderCode).orElse(null);

        model.addAttribute("payment", payment);
        model.addAttribute("bankCode", sepayService.getBankCode());
        model.addAttribute("accountNumber", sepayService.getAccountNumber());
        model.addAttribute("accountName", sepayService.getAccountName());
        model.addAttribute("currentUser", SecurityUtils.getCurrentUser());

        return "payment/show";
    }
}
