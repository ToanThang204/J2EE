package com.hutech.demo.repository;

import com.hutech.demo.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findBySepayOrderId(String sepayOrderId);
    List<Payment> findByUserId(Long userId);
    List<Payment> findByCompany_Id(Long companyId);
    List<Payment> findByStatus(String status);
}
