package com.demo.talentbridge.service.impl;

import com.demo.talentbridge.dto.request.SkillRequest;
import com.demo.talentbridge.dto.response.SkillResponse;
import com.demo.talentbridge.entity.Skill;
import com.demo.talentbridge.exception.DuplicateResourceException;
import com.demo.talentbridge.exception.ResourceNotFoundException;
import com.demo.talentbridge.repository.SkillRepository;
import com.demo.talentbridge.service.SkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SkillServiceImpl implements SkillService {
    @Autowired private SkillRepository skillRepository;

    @Override @Transactional
    public SkillResponse createSkill(SkillRequest request) {
        if (skillRepository.existsByName(request.getName()))
            throw new DuplicateResourceException("Skill already exists: " + request.getName());
        Skill s = Skill.builder().name(request.getName()).build();
        return map(skillRepository.save(s));
    }

    @Override @Transactional
    public SkillResponse updateSkill(Long id, SkillRequest request) {
        Skill s = skillRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Skill","id",id));
        s.setName(request.getName());
        return map(skillRepository.save(s));
    }

    @Override @Transactional
    public void deleteSkill(Long id) {
        skillRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Skill","id",id));
        skillRepository.deleteById(id);
    }

    @Override
    public SkillResponse getSkillById(Long id) {
        return map(skillRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Skill","id",id)));
    }

    @Override
    public List<SkillResponse> getAllSkills() {
        return skillRepository.findAll().stream().map(this::map).collect(Collectors.toList());
    }

    private SkillResponse map(Skill s) {
        return SkillResponse.builder().id(s.getId()).name(s.getName()).build();
    }
}
