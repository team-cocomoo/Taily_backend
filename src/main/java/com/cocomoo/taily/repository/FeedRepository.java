package com.cocomoo.taily.repository;

import com.cocomoo.taily.entity.Feed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedRepository extends JpaRepository<Feed, Long> {
    // 유저 프로필 피드 개수
    @Query("SELECT COUNT(f) FROM Feed f WHERE f.user.id = :userId")
    Long countFeedsByUserId(Long userId);
}
