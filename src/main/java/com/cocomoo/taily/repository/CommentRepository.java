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

    // 부모가 없는 루트 댓글 + 유저 페치 (페이징)
    @Query(
            value = """
            select c from Comment c
            join fetch c.usersId u
            where c.postsId = :postsId
              and c.tableTypesId.id = :tableTypeId
              and c.parentCommentsId is null
            order by c.createdAt asc
        """,
            countQuery = """
            select count(c) from Comment c
            where c.postsId = :postsId
              and c.tableTypesId.id = :tableTypeId
              and c.parentCommentsId is null
        """
    )
    Page<Comment> findRootCommentsByPostAndTableWithUser(
            @Param("postsId") Long postsId,
            @Param("tableTypeId") Long tableTypeId,
            Pageable pageable
    );

    // 해당 글의 모든 댓글 + 유저 페치 (대댓글 트리 구성용)
    @Query("""
        select c from Comment c
        join fetch c.usersId u
        where c.postsId = :postsId
          and c.tableTypesId.id = :tableTypeId
        order by c.createdAt asc
    """)
    List<Comment> findAllCommentsByPostAndTableWithUser(
            @Param("postsId") Long postsId,
            @Param("tableTypeId") Long tableTypeId
    );
}
