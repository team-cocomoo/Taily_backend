package com.cocomoo.taily.repository;

import com.cocomoo.taily.entity.Comment;
import com.cocomoo.taily.entity.TableType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT c FROM Comment c JOIN FETCH c.usersId WHERE c.postsId = :postId AND c.parentCommentsId IS NULL")
    Page<Comment> findByPostsIdAndParentCommentsIdIsNullWithUser(@Param("postId") Long postId, Pageable pageable);

    @Query("SELECT c FROM Comment c JOIN FETCH c.usersId WHERE c.postsId = :postId")
    List<Comment> findByPostsIdWithUser(@Param("postId") Long postId);
}
