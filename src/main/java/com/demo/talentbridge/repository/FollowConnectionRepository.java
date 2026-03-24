package com.demo.talentbridge.repository;

import com.demo.talentbridge.entity.FollowConnection;
import org.springframework.data.jpa.repository.EntityGraph;
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

    @EntityGraph(attributePaths = {"follower", "followed"})
    List<FollowConnection> findByFollowerIdOrderByFollowedAtDesc(Long followerId);

    @EntityGraph(attributePaths = {"follower", "followed"})
    List<FollowConnection> findByFollowedIdOrderByFollowedAtDesc(Long followedId);

    long countByFollowedId(Long followedId);

    long countByFollowerId(Long followerId);

    @Query("SELECT fc.follower.id FROM FollowConnection fc WHERE fc.followed.id = :followedId")
    List<Long> findFollowerIdsByFollowedId(@Param("followedId") Long followedId);
}