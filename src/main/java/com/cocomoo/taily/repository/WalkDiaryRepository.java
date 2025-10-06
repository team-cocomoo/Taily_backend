package com.cocomoo.taily.repository;

import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.entity.WalkDiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalkDiaryRepository extends JpaRepository<WalkDiary, Long> {
    /**
     * 달 마다 특정 날짜에 작성한 산책 일지를 작성자 정보와 함께 조회
     *
     * - N+1 문제 방지 (fetch join으로 한 번에 조회)
     */
    @Query("SELECT w FROM WalkDiary w JOIN FETCH w.user u WHERE u.username = :username AND w.date BETWEEN :start AND :end ORDER BY w.date")
    List<WalkDiary> findByMonth(
            @Param("username") String username,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("SELECT w FROM WalkDiary w JOIN FETCH w.user WHERE w.id = :id")
    Optional<WalkDiary> findByIdWithUser(Long id);

    List<WalkDiary> findAllByUserAndDateBetween(User user, LocalDate startDate, LocalDate endDate);

    boolean existsByUserAndDate(User user, LocalDate date);
}
