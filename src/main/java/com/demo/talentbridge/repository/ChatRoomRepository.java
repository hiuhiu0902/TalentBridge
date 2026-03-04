package com.demo.talentbridge.repository;

import com.demo.talentbridge.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("SELECT cr FROM ChatRoom cr WHERE " +
           "(cr.userOne.id = :userOneId AND cr.userTwo.id = :userTwoId) OR " +
           "(cr.userOne.id = :userTwoId AND cr.userTwo.id = :userOneId)")
    Optional<ChatRoom> findByUsers(@Param("userOneId") Long userOneId, @Param("userTwoId") Long userTwoId);

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.userOne.id = :userId OR cr.userTwo.id = :userId " +
           "ORDER BY cr.lastMessageAt DESC NULLS LAST")
    List<ChatRoom> findByUserId(@Param("userId") Long userId);
}
