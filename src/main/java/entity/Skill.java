package entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "skills")
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @OneToMany(mappedBy = "skill")
    private List<JobSkill> jobSkills = new ArrayList<>();

    @OneToMany(mappedBy = "skill")
    private List<CandidateSkill> candidateSkills = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<JobSkill> getJobSkills() { return jobSkills; }
    public void setJobSkills(List<JobSkill> jobSkills) { this.jobSkills = jobSkills; }
    public List<CandidateSkill> getCandidateSkills() { return candidateSkills; }
    public void setCandidateSkills(List<CandidateSkill> candidateSkills) { this.candidateSkills = candidateSkills; }
}
