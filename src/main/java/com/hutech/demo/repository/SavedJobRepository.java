package com.hutech.demo.repository;

import com.hutech.demo.model.SavedJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {
    List<SavedJob> findByUser_Id(Long userId);
    Optional<SavedJob> findByUser_IdAndJob_Id(Long userId, Long jobId);
    boolean existsByUser_IdAndJob_Id(Long userId, Long jobId);
}
