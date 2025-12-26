package com.hutech.demo.service;

import com.hutech.demo.model.SavedJob;
import com.hutech.demo.repository.SavedJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SavedJobService {
    private final SavedJobRepository savedJobRepository;

    public List<SavedJob> getSavedJobsByUser(Long userId) {
        return savedJobRepository.findByUser_Id(userId);
    }

    public boolean isJobSavedByUser(Long userId, Long jobId) {
        return savedJobRepository.existsByUser_IdAndJob_Id(userId, jobId);
    }

    @Transactional
    public SavedJob saveJob(SavedJob savedJob) {
        if (savedJobRepository.existsByUser_IdAndJob_Id(
                savedJob.getUser().getId(), 
                savedJob.getJob().getId())) {
            throw new RuntimeException("Job already saved");
        }
        savedJob.setSavedAt(LocalDateTime.now());
        return savedJobRepository.save(savedJob);
    }

    @Transactional
    public void unsaveJob(Long userId, Long jobId) {
        Optional<SavedJob> savedJob = savedJobRepository.findByUser_IdAndJob_Id(userId, jobId);
        savedJob.ifPresent(savedJobRepository::delete);
    }

    @Transactional
    public void deleteSavedJob(Long id) {
        savedJobRepository.deleteById(id);
    }
}
