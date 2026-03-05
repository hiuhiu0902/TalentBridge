package com.demo.talentbridge.repository;

import com.demo.talentbridge.entity.Interview;
import com.demo.talentbridge.enums.InterviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    List<Interview> findByApplicationId(Long applicationId);

    @Query("SELECT i FROM Interview i WHERE i.application.candidate.user.id = :userId ORDER BY i.interviewAt DESC")
    List<Interview> findByApplicationCandidateUserId(@Param("userId") Long userId);

    @Query("SELECT i FROM Interview i WHERE i.application.jobPost.employer.user.id = :userId ORDER BY i.interviewAt DESC")
    List<Interview> findByApplicationJobPostEmployerUserId(@Param("userId") Long userId);

    @Query("SELECT i FROM Interview i WHERE i.application.id = :applicationId AND i.status = :status")
    List<Interview> findByApplicationIdAndStatus(@Param("applicationId") Long applicationId,
                                                  @Param("status") InterviewStatus status);
}
