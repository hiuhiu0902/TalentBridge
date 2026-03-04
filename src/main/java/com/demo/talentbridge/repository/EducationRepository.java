package com.demo.talentbridge.repository;

import com.demo.talentbridge.entity.Education;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EducationRepository extends JpaRepository<Education, Long> {
    List<Education> findByCandidateId(Long candidateId);
}
