package com.demo.talentbridge.repository;

import com.demo.talentbridge.entity.CandidateSkill;
import com.demo.talentbridge.enums.SkillName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateSkillRepository extends JpaRepository<CandidateSkill, Long> {
    List<CandidateSkill> findByCandidateId(Long candidateId);
    Optional<CandidateSkill> findByCandidateIdAndSkillName(Long candidateId, SkillName skillName);
    boolean existsByCandidateIdAndSkillName(Long candidateId, SkillName skillName);
    void deleteByCandidateId(Long candidateId);
}
