package com.cocomoo.taily.repository;

import com.cocomoo.taily.entity.Alarm;
import com.cocomoo.taily.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlarmRepository extends JpaRepository<Alarm, Long> {
    // 수신자 기준으로 최신순 정렬
    List<Alarm> findByReceiverOrderByCreatedAtDesc(User user);
}
