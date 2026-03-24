package com.demo.talentbridge.repository;

import com.demo.talentbridge.entity.ApplicationHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationHistoryRepository extends JpaRepository<ApplicationHistory, Long> {
    List<ApplicationHistory> findByApplicationIdOrderByChangedAtAsc(Long applicationId);
    @Query("""
    select h
    from ApplicationHistory h
    left join fetch h.changedBy
    where h.application.id = :applicationId
    order by h.changedAt asc
""")
    List<ApplicationHistory> findByApplicationIdWithChangedBy(@Param("applicationId") Long applicationId);
}
