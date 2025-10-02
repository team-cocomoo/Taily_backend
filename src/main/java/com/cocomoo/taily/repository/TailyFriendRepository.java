package com.cocomoo.taily.repository;

import com.cocomoo.taily.entity.TailyFriend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TailyFriendRepository extends JpaRepository<TailyFriend, Long> {

}
