//package com.demo.talentbridge.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Entity
//@Table(name = "skills")
//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class Skill {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false, unique = true, length = 100)
//    private String name;
//
//    @OneToMany(mappedBy = "skill")
//    @Builder.Default
//    private List<JobSkill> jobSkills = new ArrayList<>();
//
//    @OneToMany(mappedBy = "skill")
//    @Builder.Default
//    private List<CandidateSkill> candidateSkills = new ArrayList<>();
//}
