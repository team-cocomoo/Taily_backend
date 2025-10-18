package com.cocomoo.taily.repository;

import com.cocomoo.taily.entity.Comment;
import com.cocomoo.taily.entity.TailyFriend;
import com.cocomoo.taily.entity.WalkPath;
import com.cocomoo.taily.entity.WalkPathRoute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalkPathRepository extends JpaRepository<WalkPath,Long>{

    //전체 게시글 조회
    @Query("SELECT w FROM WalkPath w JOIN FETCH w.user")
    List<WalkPath> findAllWithUser();

    //특정 게시글 조회
    @Query("SELECT w FROM WalkPath w JOIN FETCH w.user WHERE w.id = :id")
    Optional<WalkPath> findByIdWithUser(Long id);

    //키워드로 게시글 조회
    @Query("SELECT w FROM WalkPath w WHERE " +
            "LOWER(w.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(w.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY w.createdAt DESC")
    Page<WalkPath> searchByKeyword(String keyword, PageRequest of);

    @Query("SELECT w FROM WalkPath w ORDER BY w.createdAt DESC")
    Page<WalkPath> findAllWithUser(Pageable pageable);

    @Query("SELECT COUNT(w) FROM WalkPath w JOIN w.user u WHERE u.id = :userId")
    Long countWalkPathsByUserId(@Param("userId") Long userId);

    List<WalkPath> findAllByIdIn(List<Long> walkPathIds);

    Comment getCommentById(Long parentCommentId);

    //게시글 id 별 경로지점 검색
    @Query("SELECT r FROM WalkPathRoute r WHERE r.walkPath.id = :walkPathId ORDER BY r.orderNo ASC")
    List<WalkPathRoute> findByWalkPathId(Long walkPathId);
}
