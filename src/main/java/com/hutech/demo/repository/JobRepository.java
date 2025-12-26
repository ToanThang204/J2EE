package com.hutech.demo.repository;

import com.hutech.demo.model.Job;
import com.hutech.demo.model.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByStatus(JobStatus status);
    List<Job> findByCompany_Id(Long companyId);
    List<Job> findByTitleContaining(String title);
    List<Job> findByLocationContaining(String location);
}
