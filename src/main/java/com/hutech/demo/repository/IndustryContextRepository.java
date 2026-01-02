package com.hutech.demo.repository;

import com.hutech.demo.model.IndustryContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IndustryContextRepository extends JpaRepository<IndustryContext, Long> {
    
    /**
     * Tìm industry context theo key (it, marketing, sales...)
     */
    Optional<IndustryContext> findByKey(String key);
    
    /**
     * Lấy tất cả active contexts
     */
    List<IndustryContext> findByIsActiveTrue();
    
    /**
     * Check key đã tồn tại chưa
     */
    boolean existsByKey(String key);
}
