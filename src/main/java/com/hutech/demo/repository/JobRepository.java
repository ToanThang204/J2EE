package com.hutech.demo.repository;

import com.hutech.demo.model.Job;
import com.hutech.demo.model.enums.JobStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    @EntityGraph(attributePaths = {"company"})
    List<Job> findByStatus(JobStatus status);
    
    @Query("SELECT j FROM Job j LEFT JOIN FETCH j.company WHERE j.company.id = :companyId")
    List<Job> findByCompany_Id(@Param("companyId") Long companyId);
    
    
    @EntityGraph(attributePaths = {"company"})
    List<Job> findByTitleContaining(String title);
    
    @EntityGraph(attributePaths = {"company"})
    List<Job> findByLocationContaining(String location);
    
    @EntityGraph(attributePaths = {"company"})
    @Override
    List<Job> findAll();
}
