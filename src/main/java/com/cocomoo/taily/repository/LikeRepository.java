package com.cocomoo.taily.repository;

import com.cocomoo.taily.entity.Like;
import com.cocomoo.taily.entity.TableType;
import com.cocomoo.taily.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByPostsIdAndTableTypesIdAndUsersIdAndState(Long postsId, TableType tableType, User user, Boolean state);
    boolean existsByPostsIdAndTableTypesIdAndUsersIdAndState(Long postsId, TableType tableType, User user, Boolean state);
}
