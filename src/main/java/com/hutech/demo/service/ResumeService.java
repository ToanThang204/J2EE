package com.hutech.demo.service;

import com.hutech.demo.model.Resume;
import com.hutech.demo.model.User;
import com.hutech.demo.repository.ResumeRepository;
import com.hutech.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeService {
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;

    public List<Resume> getAllResumes() {
        return resumeRepository.findAll();
    }

    public Optional<Resume> getResumeById(Long id) {
        return resumeRepository.findById(id);
    }

    public List<Resume> getResumesByUser(Long userId) {
        log.info("Getting resumes for user ID: {}", userId);
        List<Resume> resumes = resumeRepository.findByUser_Id(userId);
        log.info("Found {} resumes for user {}", resumes.size(), userId);
        return resumes;
    }

    @Transactional
    public Resume createResume(Resume resume) {
        Long userId = resume.getUserId();
        log.info("Creating resume - userId from resume: {}", userId);
        
        // Nếu không có userId trong resume, có thể do frontend không gửi
        // Thử lấy từ SecurityContext
        if (userId == null) {
            log.warn("UserId is null in resume object, this should not happen!");
            throw new RuntimeException("User ID is required to create resume");
        }
        
        log.debug("Loading user with ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", userId);
                    return new RuntimeException("User not found with id: " + userId);
                });
        log.debug("User loaded: {} ({})", user.getEmail(), user.getName());
        resume.setUser(user);
        
        log.debug("Saving resume to database...");
        log.debug("Resume data: title={}, fullName={}, email={}, phone={}", 
            resume.getTitle(), resume.getFullName(), resume.getEmail(), resume.getPhone());
        
        Resume saved = resumeRepository.save(resume);
        log.info("Resume saved successfully with ID: {}", saved.getId());
        return saved;
    }

    @Transactional
    public Resume updateResume(Long id, Resume resumeDetails) {
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        
        resume.setTitle(resumeDetails.getTitle());
        resume.setFullName(resumeDetails.getFullName());
        resume.setEmail(resumeDetails.getEmail());
        resume.setPhone(resumeDetails.getPhone());
        resume.setAddress(resumeDetails.getAddress());
        resume.setSummary(resumeDetails.getSummary());
        resume.setPhoto(resumeDetails.getPhoto());
        resume.setPersonalInfo(resumeDetails.getPersonalInfo());
        resume.setSkillsSummary(resumeDetails.getSkillsSummary());
        
        return resumeRepository.save(resume);
    }

    @Transactional
    public void deleteResume(Long id) {
        resumeRepository.deleteById(id);
    }
}
