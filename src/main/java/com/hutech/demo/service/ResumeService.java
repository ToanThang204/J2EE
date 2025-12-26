package com.hutech.demo.service;

import com.hutech.demo.model.Resume;
import com.hutech.demo.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResumeService {
    private final ResumeRepository resumeRepository;

    public List<Resume> getAllResumes() {
        return resumeRepository.findAll();
    }

    public Optional<Resume> getResumeById(Long id) {
        return resumeRepository.findById(id);
    }

    public List<Resume> getResumesByUser(Long userId) {
        return resumeRepository.findByUser_Id(userId);
    }

    @Transactional
    public Resume createResume(Resume resume) {
        return resumeRepository.save(resume);
    }

    @Transactional
    public Resume updateResume(Long id, Resume resumeDetails) {
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        
        resume.setTitle(resumeDetails.getTitle());
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
