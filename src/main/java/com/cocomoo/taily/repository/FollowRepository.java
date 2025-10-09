package com.cocomoo.taily.repository;

import com.cocomoo.taily.entity.Follow;
import com.cocomoo.taily.entity.FollowState;
import com.cocomoo.taily.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    // 특정 사용자가 팔로잉 중인 리스트
    List<Follow> findByFollowerAndState(User follower, FollowState state);

    // 특정 사용자를 팔로우한 리스트
    List<Follow> findByFollowingAndState(User following, FollowState state);

    // 특정 팔로우 관계 조회 (팔로워 → 팔로잉)
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);

    // 팔로워 수 (나를 팔로우한 사람)
    @Query("SELECT COUNT(f) FROM Follow f WHERE f.following.id = :userId AND f.state = 'ACTIVE'")
    Long countFollowers(Long userId);

    // 팔로잉 수 (내가 팔로우한 사람)
    @Query("SELECT COUNT(f) FROM Follow f WHERE f.follower.id = :userId AND f.state = 'ACTIVE'")
    Long countFollowings(Long userId);

}
