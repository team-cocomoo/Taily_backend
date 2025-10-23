package com.cocomoo.taily.repository;

import com.cocomoo.taily.entity.MessageRoom;
import com.cocomoo.taily.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MessageRoomRepository extends JpaRepository<MessageRoom, Long> {
    @Query("SELECT m FROM MessageRoom m " +
            "JOIN FETCH m.user1 u1 " +
            "JOIN FETCH m.user2 u2 " +
            "WHERE u1.username = :username OR u2.username = :username")
    List<MessageRoom> findByUsernameWithUsers(@Param("username") String username);

    @Query(" SELECT m FROM MessageRoom m " +
            "WHERE (m.user1 = :user1 AND m.user2 = :user2)" +
            "OR (m.user1 = :user2 AND m.user2 = :user1)")
    Optional<MessageRoom> findByUsers(@Param("user1") User user1, @Param("user2") User user2);
}
