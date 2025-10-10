package com.cocomoo.taily.repository;

import com.cocomoo.taily.entity.Comment;
import com.cocomoo.taily.entity.TableType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostsId(Long postId);

    Page<Comment> findByPostsIdAndParentCommentsIdIsNull(Long postId, Pageable pageable);
}
