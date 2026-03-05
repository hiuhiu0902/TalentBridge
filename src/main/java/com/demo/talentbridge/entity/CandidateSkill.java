package com.demo.talentbridge.entity;

import com.demo.talentbridge.enums.SkillLevel;
import com.demo.talentbridge.enums.SkillName;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "candidate_skills",
        uniqueConstraints = @UniqueConstraint(columnNames = {"candidate_id", "skill_name"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_name", nullable = false, length = 100)
    private SkillName skillName;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private SkillLevel level;
}
