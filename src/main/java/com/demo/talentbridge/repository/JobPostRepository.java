package com.demo.talentbridge.repository;

import com.demo.talentbridge.entity.JobPost;
import com.demo.talentbridge.enums.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobPostRepository extends JpaRepository<JobPost, Long> {

    List<JobPost> findByEmployerId(Long employerId);

    List<JobPost> findByStatus(JobStatus status);

    Page<JobPost> findByStatus(JobStatus status, Pageable pageable);

    @Query("SELECT j FROM JobPost j WHERE j.status = 'ACTIVE' AND " +
           "(LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<JobPost> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT j FROM JobPost j WHERE j.status = 'ACTIVE' AND j.category.id = :categoryId")
    Page<JobPost> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    // Feed: Jobs from followed employers first, then other active jobs
    @Query("SELECT j FROM JobPost j WHERE j.status = 'ACTIVE' AND j.employer.id IN " +
           "(SELECT fc.followed.id FROM FollowConnection fc WHERE fc.follower.id = :userId) " +
           "ORDER BY j.postedAt DESC")
    List<JobPost> findJobsFromFollowedEmployers(@Param("userId") Long userId);

    @Query("SELECT j FROM JobPost j WHERE j.status = 'ACTIVE' ORDER BY j.postedAt DESC")
    Page<JobPost> findActiveFeed(Pageable pageable);

    boolean existsByIdAndEmployerId(Long id, Long employerId);
}
