package com.demo.talentbridge.repository;

import com.demo.talentbridge.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);

    @Query("""
        select distinct u
        from User u
        left join u.employer e
        where u.active = true
          and (
                lower(u.username) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(u.fullName, '')) like lower(concat('%', :keyword, '%'))
                or lower(u.email) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(e.companyName, '')) like lower(concat('%', :keyword, '%'))
          )
        order by u.createdAt desc
    """)
    List<User> searchUsers(@Param("keyword") String keyword);

    @Query("""
        select distinct u
        from User u
        left join u.employer e
        where u.active = true
          and (
                lower(u.username) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(u.fullName, '')) like lower(concat('%', :keyword, '%'))
                or lower(u.email) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(e.companyName, '')) like lower(concat('%', :keyword, '%'))
          )
    """)
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);

    List<User> findTop20ByActiveTrueOrderByCreatedAtDesc();

    List<User> findTop50ByActiveTrueAndIdNotOrderByCreatedAtDesc(Long currentUserId);

    @Query("""
        select u
        from User u
        left join fetch u.employer
        left join fetch u.candidate
        where u.id = :userId
          and u.active = true
    """)
    Optional<User> findActiveProfileById(@Param("userId") Long userId);
}
