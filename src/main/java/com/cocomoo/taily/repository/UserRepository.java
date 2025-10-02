package com.cocomoo.taily.repository;

import com.cocomoo.taily.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * username으로 회원 조회 - Spring Security 인증 시 필수
     * Spring Security에서 사용 예:
     * UserDetailsService.loadUserByUsername() 구현 시 활용
     *
     * @param username 로그인 ID
     * @return Optional<Member> 회원 정보
     */
    Optional<User> findByUsername(String username);
    /**
     * username 중복 체크 - 회원가입 시 필수
     *
     * 쿼리 메서드 규칙:
     * - exists: boolean 반환
     * - By: WHERE 조건
     * - Username: 필드명
     *
     * 자동 생성되는 SQL:
     * SELECT COUNT(*) > 0 FROM members WHERE username = ?
     *
     * @param username 체크할 로그인 ID
     * @return true: 이미 존재, false: 사용 가능
     */
    boolean existsByUsername(String username);
}
