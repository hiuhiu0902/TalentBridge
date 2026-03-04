package com.demo.talentbridge.repository;

import com.demo.talentbridge.entity.FollowConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowConnectionRepository extends JpaRepository<FollowConnection, Long> {

    boolean existsByFollowerIdAndFollowedId(Long followerId, Long followedId);

    Optional<FollowConnection> findByFollowerIdAndFollowedId(Long followerId, Long followedId);

    List<FollowConnection> findByFollowerId(Long followerId);

    List<FollowConnection> findByFollowedId(Long followedId);

    long countByFollowedId(Long followedId);

    long countByFollowerId(Long followerId);

    // Get all follower user IDs for a given followed user
    @Query("SELECT fc.follower.id FROM FollowConnection fc WHERE fc.followed.id = :followedId")
    List<Long> findFollowerIdsByFollowedId(@Param("followedId") Long followedId);
}
