package com.cocomoo.taily.repository;

import com.cocomoo.taily.entity.WalkDiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalkDiaryRepository extends JpaRepository<WalkDiary, Long> {
}
