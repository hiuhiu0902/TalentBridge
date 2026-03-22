package com.demo.talentbridge.repository;

import com.demo.talentbridge.entity.CandidateSkill;
import com.demo.talentbridge.enums.SkillName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateSkillRepository extends JpaRepository<CandidateSkill, Long> {
    List<CandidateSkill> findByCandidateId(Long candidateId);
    Optional<CandidateSkill> findByCandidateIdAndSkillName(Long candidateId, SkillName skillName);
    boolean existsByCandidateIdAndSkillName(Long candidateId, SkillName skillName);
    void deleteByCandidateId(Long candidateId);

    @Query("SELECT cs.candidate.user.id FROM CandidateSkill cs WHERE cs.skillName IN :skillNames " +
            "GROUP BY cs.candidate.user.id HAVING COUNT(DISTINCT cs.skillName) >= :minMatches")
    List<Long> findCandidateUserIdsMatchingSkills(@Param("skillNames") List<SkillName> skillNames,
                                                  @Param("minMatches") long minMatches);

}
