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
    @Query("""
    select distinct a
    from Application a
    join fetch a.candidate c
    join fetch c.user cu
    join fetch a.jobPost jp
    join fetch jp.employer e
    where c.id = :candidateId
""")
    List<Application> findByCandidateIdWithDetails(@Param("candidateId") Long candidateId);
    @Query("""
    select distinct a
    from Application a
    join fetch a.candidate c
    join fetch c.user cu
    join fetch a.jobPost jp
    join fetch jp.employer e
    where e.id = :employerId
""")
    List<Application> findByEmployerIdWithDetails(@Param("employerId") Long employerId);
    @Query("""
    select distinct a
    from Application a
    join fetch a.candidate c
    join fetch c.user cu
    join fetch a.jobPost jp
    join fetch jp.employer e
    where jp.id = :jobPostId
""")
    List<Application> findByJobPostIdWithDetails(@Param("jobPostId") Long jobPostId);
    @Query("""
    select a
    from Application a
    join fetch a.candidate c
    join fetch c.user cu
    join fetch a.jobPost jp
    join fetch jp.employer e
    where a.id = :applicationId
""")
    Optional<Application> findByIdWithDetails(@Param("applicationId") Long applicationId);
}
