package com.demo.talentbridge.entity;

import com.demo.talentbridge.enums.SkillLevel;
import com.demo.talentbridge.enums.SkillName;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "job_skills",
        uniqueConstraints = @UniqueConstraint(columnNames = {"job_post_id", "skill_name"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_post_id", nullable = false)
    private JobPost jobPost;

    @Enumerated(EnumType.STRING)
    @Column(name = "skill_name", nullable = false, length = 100)
    private SkillName skillName;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private SkillLevel level;
}
