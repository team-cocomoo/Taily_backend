package com.cocomoo.taily.repository;

import com.cocomoo.taily.entity.Comment;
import com.cocomoo.taily.entity.TailyFriend;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TailyFriendRepository extends JpaRepository<TailyFriend, Long> {
    @Query("SELECT t FROM TailyFriend t JOIN FETCH t.user WHERE t.id = :id")
    Optional<TailyFriend> findByIdWithUser(Long id);

    @Query("SELECT t FROM TailyFriend t " +
            "WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(t.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(t.address) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY t.createdAt DESC")
    Page<TailyFriend> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT t FROM TailyFriend t ORDER BY t.createdAt DESC")
    Page<TailyFriend> findAllWithUser(Pageable pageable);

    @Query("SELECT t.address FROM TailyFriend t")
    List<String> findAllAddresses();

    @Query("SELECT COUNT(t) FROM TailyFriend t JOIN t.user u WHERE u.id = :userId")
    Long countTailyFriendsByUserId(@Param("userId") Long userId);

    Page<TailyFriend> findByUserId(Long userId, Pageable pageable);

    List<TailyFriend> findAllByIdIn(List<Long> tailyFriendsIds);

    @Query("SELECT c FROM Comment c WHERE c.id = :commentId")
    Comment getCommentById(@Param("commentId") Long parentCommentId);

    @Query("SELECT tf FROM TailyFriend tf JOIN FETCH tf.user u " +
            "WHERE (:keyword IS NULL OR tf.title LIKE %:keyword% OR tf.content LIKE %:keyword% OR u.nickname LIKE %:keyword%) ORDER BY tf.createdAt ASC")
    Page<TailyFriend> findAllWithUserAndKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Modifying
    @Query("UPDATE TailyFriend tf SET tf.view = tf.view + 1 WHERE tf.id = :id")
    void incrementViewCount(@Param("id") Long id);


}
