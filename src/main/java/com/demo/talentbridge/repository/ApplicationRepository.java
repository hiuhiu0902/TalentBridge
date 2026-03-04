package com.demo.talentbridge.repository;

import com.demo.talentbridge.entity.Application;
import com.demo.talentbridge.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByCandidateIdAndJobPostId(Long candidateId, Long jobPostId);

    List<Application> findByCandidateId(Long candidateId);

    List<Application> findByJobPostId(Long jobPostId);

    List<Application> findByJobPostIdAndStatus(Long jobPostId, ApplicationStatus status);

    Optional<Application> findByCandidateIdAndJobPostId(Long candidateId, Long jobPostId);

    @Query("SELECT a FROM Application a WHERE a.jobPost.employer.id = :employerId")
    List<Application> findByEmployerId(@Param("employerId") Long employerId);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.jobPost.id = :jobPostId")
    long countByJobPostId(@Param("jobPostId") Long jobPostId);
}
