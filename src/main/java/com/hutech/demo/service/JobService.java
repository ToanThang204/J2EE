package com.hutech.demo.service;

import com.hutech.demo.model.Job;
import com.hutech.demo.model.enums.JobStatus;
import com.hutech.demo.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JobService {
    private final JobRepository jobRepository;

    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    public Optional<Job> getJobById(Long id) {
        return jobRepository.findById(id);
    }

    public List<Job> getJobsByStatus(JobStatus status) {
        return jobRepository.findByStatus(status);
    }

    public List<Job> getJobsByCompany(Long companyId) {
        return jobRepository.findByCompany_Id(companyId);
    }

    public List<Job> searchJobsByTitle(String title) {
        return jobRepository.findByTitleContaining(title);
    }

    public List<Job> searchJobsByLocation(String location) {
        return jobRepository.findByLocationContaining(location);
    }

    @Transactional
    public Job createJob(Job job) {
        return jobRepository.save(job);
    }

    @Transactional
    public Job updateJob(Long id, Job jobDetails) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));
        
        job.setTitle(jobDetails.getTitle());
        job.setDescription(jobDetails.getDescription());
        job.setRequirements(jobDetails.getRequirements());
        job.setSalaryRange(jobDetails.getSalaryRange());
        job.setLocation(jobDetails.getLocation());
        job.setEmploymentType(jobDetails.getEmploymentType());
        job.setPostedDate(jobDetails.getPostedDate());
        job.setExpirationDate(jobDetails.getExpirationDate());
        job.setStatus(jobDetails.getStatus());
        
        return jobRepository.save(job);
    }

    @Transactional
    public void deleteJob(Long id) {
        jobRepository.deleteById(id);
    }
}
