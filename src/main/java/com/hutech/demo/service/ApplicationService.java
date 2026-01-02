package com.hutech.demo.service;

import com.hutech.demo.model.Application;
import com.hutech.demo.model.enums.ApplicationStatus;
import com.hutech.demo.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final EmailService emailService;

    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }

    public Optional<Application> getApplicationById(Long id) {
        return applicationRepository.findById(id);
    }

    public List<Application> getApplicationsByUser(Long userId) {
        return applicationRepository.findByUser_Id(userId);
    }

    public List<Application> getApplicationsByJob(Long jobId) {
        return applicationRepository.findByJobId(jobId);
    }

    public List<Application> getApplicationsByStatus(ApplicationStatus status) {
        return applicationRepository.findByStatus(status);
    }

    public List<Application> getApplicationsByCompany(Long companyId) {
        return applicationRepository.findByJobCompanyId(companyId);
    }

    @Transactional
    public Application createApplication(Application application) {
        application.setAppliedAt(LocalDateTime.now());
        Application savedApplication = applicationRepository.save(application);
        
        // Gửi email cho ứng viên xác nhận đã nhận đơn
        try {
            if (savedApplication.getUser() != null && savedApplication.getUser().getEmail() != null) {
                String jobTitle = savedApplication.getJob() != null ? savedApplication.getJob().getTitle() : "Vị trí tuyển dụng";
                String companyName = savedApplication.getJob() != null && savedApplication.getJob().getCompany() != null 
                    ? savedApplication.getJob().getCompany().getCompanyName() : "Công ty";
                emailService.sendApplicationSubmittedEmail(savedApplication.getUser().getEmail(), jobTitle, companyName);
            }
            
            // Gửi email cho nhà tuyển dụng thông báo có đơn ứng tuyển mới
            if (savedApplication.getJob() != null && savedApplication.getJob().getCompany() != null 
                && savedApplication.getJob().getCompany().getEmail() != null) {
                String candidateName = savedApplication.getUser() != null && savedApplication.getUser().getName() != null 
                    ? savedApplication.getUser().getName() : "Ứng viên";
                String jobTitle = savedApplication.getJob().getTitle();
                emailService.sendNewApplicationReceivedEmail(
                    savedApplication.getJob().getCompany().getEmail(), 
                    candidateName, 
                    jobTitle
                );
            }
        } catch (Exception e) {
            // Log error nhưng không ảnh hưởng đến việc tạo application
            System.err.println("Error sending application email: " + e.getMessage());
        }
        
        return savedApplication;
    }

    @Transactional
    public Application updateApplicationStatus(Long id, ApplicationStatus status) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        
        application.setStatus(status);
        Application updatedApplication = applicationRepository.save(application);
        
        // Gửi email thông báo cập nhật trạng thái cho ứng viên
        try {
            if (updatedApplication.getUser() != null && updatedApplication.getUser().getEmail() != null) {
                String jobTitle = updatedApplication.getJob() != null ? updatedApplication.getJob().getTitle() : "Vị trí tuyển dụng";
                String statusText = status != null ? status.name() : "UNKNOWN";
                emailService.sendApplicationStatusEmail(
                    updatedApplication.getUser().getEmail(), 
                    jobTitle, 
                    statusText
                );
            }
        } catch (Exception e) {
            // Log error nhưng không ảnh hưởng đến việc cập nhật
            System.err.println("Error sending status update email: " + e.getMessage());
        }
        
        return updatedApplication;
    }

    @Transactional
    public void deleteApplication(Long id) {
        applicationRepository.deleteById(id);
    }
}
