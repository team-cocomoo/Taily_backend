package com.cocomoo.taily.repository;

import com.cocomoo.taily.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 필요하다면 추가 메서드 정의 가능
    User findByUsername(String username);
    boolean existsByEmail(String email);
}
