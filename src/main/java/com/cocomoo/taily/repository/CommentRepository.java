package com.cocomoo.taily.repository;

import com.cocomoo.taily.entity.Comment;
import com.cocomoo.taily.entity.TableType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostsId(Long postId);
}
