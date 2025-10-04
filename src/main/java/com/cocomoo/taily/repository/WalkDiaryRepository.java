package com.cocomoo.taily.repository;

import com.cocomoo.taily.entity.WalkDiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalkDiaryRepository extends JpaRepository<WalkDiary, Long> {
    /**
     * 모든 게시글을 작성자 정보와 함께 조회
     *
     * - N+1 문제 방지 (fetch join으로 한 번에 조회)
     * - 게시글 목록을 보여줄 때 작성자 이름도 필요
     */
    @Query("SELECT w FROM WalkDiary w JOIN FETCH w.user ORDER BY w.createdAt ASC")
    List<WalkDiary> findAllWithUser();
}
