package com.demo.talentbridge.repository;

import com.demo.talentbridge.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    Page<ChatMessage> findByChatRoomIdOrderBySentAtAsc(Long roomId, Pageable pageable);

    List<ChatMessage> findByChatRoomIdOrderBySentAtAsc(Long roomId);

    @Query("SELECT cm FROM ChatMessage cm WHERE cm.chatRoom.id = :roomId ORDER BY cm.sentAt DESC LIMIT 1")
    Optional<ChatMessage> findLastMessageByRoomId(@Param("roomId") Long roomId);

    long countByChatRoomIdAndIsReadFalseAndSenderIdNot(Long roomId, Long senderId);

    @Modifying
    @Query("UPDATE ChatMessage cm SET cm.isRead = true WHERE cm.chatRoom.id = :roomId AND cm.sender.id != :userId")
    void markMessagesAsRead(@Param("roomId") Long roomId, @Param("userId") Long userId);
}
