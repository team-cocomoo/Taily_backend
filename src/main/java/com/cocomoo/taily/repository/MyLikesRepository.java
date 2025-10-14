package com.cocomoo.taily.repository;

import com.cocomoo.taily.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MyLikesRepository extends JpaRepository<Like, Long> {
    List<Like> findByUserUsernameAndState(String username, boolean state);
}
