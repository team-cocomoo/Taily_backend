package com.cocomoo.taily.repository;

import com.cocomoo.taily.entity.WalkDiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalkDiaryRepository extends JpaRepository<WalkDiary, Long> {
    @Query("SELECT w FROM WalkDiary w JOIN FETCH w.user ORDER BY w.createdAt ASC")
    List<WalkDiary> findAllWithUser();
}
