package com.hutech.demo.repository;

import com.hutech.demo.model.AiMatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiMatchResultRepository extends JpaRepository<AiMatchResult, Long> {
    Optional<AiMatchResult> findByUserIdAndJobIdAndResumeId(Long userId, Long jobId, Long resumeId);
    Optional<AiMatchResult> findByJobIdAndResumeIdAndUserId(Long jobId, Long resumeId, Long userId);
    List<AiMatchResult> findByUserId(Long userId);
    List<AiMatchResult> findByJobId(Long jobId);
}
