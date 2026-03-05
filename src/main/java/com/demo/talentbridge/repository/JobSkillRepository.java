package com.demo.talentbridge.repository;

import com.demo.talentbridge.entity.JobSkill;
import com.demo.talentbridge.enums.SkillName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobSkillRepository extends JpaRepository<JobSkill, Long> {
    List<JobSkill> findByJobPostId(Long jobPostId);

    @Query("SELECT DISTINCT js.jobPost.id FROM JobSkill js WHERE js.skillName IN :skillNames")
    List<Long> findJobPostIdsBySkillNames(@Param("skillNames") List<SkillName> skillNames);
}
