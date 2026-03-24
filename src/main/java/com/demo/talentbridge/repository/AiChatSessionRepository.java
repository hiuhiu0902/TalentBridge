package com.demo.talentbridge.repository;

import com.demo.talentbridge.entity.AiChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiChatSessionRepository extends JpaRepository<AiChatSession, Long> {
    List<AiChatSession> findByUserIdAndActiveTrueOrderByLastMessageAtDescCreatedAtDesc(Long userId);
    Optional<AiChatSession> findByIdAndUserIdAndActiveTrue(Long id, Long userId);
}
