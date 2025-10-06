package com.cocomoo.taily.repository;

import com.cocomoo.taily.entity.TailyFriend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TailyFriendRepository extends JpaRepository<TailyFriend, Long> {
    @Query("SELECT t FROM TailyFriend t JOIN FETCH t.user ORDER BY t.createdAt DESC")
    List<TailyFriend> findAllWithUser();

    @Query("SELECT t FROM TailyFriend t JOIN FETCH t.user WHERE t.id = :id")
    Optional<TailyFriend> findByIdWithUser(Long id);

}
