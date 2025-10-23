package com.cocomoo.taily.repository;

import com.cocomoo.taily.entity.Like;
import com.cocomoo.taily.entity.TableType;
import com.cocomoo.taily.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MyLikesRepository extends JpaRepository<Like, Long> {
    Page<Like> findByUserUsernameAndStateOrderByIdDesc(String username, boolean state, Pageable pageable);

    Optional<Like> findByUserAndPostsIdAndTableType(User user, Long postsId, TableType tableType);

}
