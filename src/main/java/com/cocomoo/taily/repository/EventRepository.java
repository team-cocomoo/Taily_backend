package com.cocomoo.taily.repository;

import com.cocomoo.taily.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    /**
     * 이벤트 상세 조회
     */
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT e FROM Event e WHERE e.id = :id")
    Optional<Event> findByIdWithUser(@Param("id") Long id);

    /**
     * 이벤트 목록 조회
     */
    Page<Event> findAll(Pageable pageable);
}
