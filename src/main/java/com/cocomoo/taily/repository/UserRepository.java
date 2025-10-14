package com.cocomoo.taily.repository;

import com.cocomoo.taily.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    // publicId로 회원 모든 정보 조회
    Optional<User> findByPublicId(String publicId);

    @Query("SELECT u FROM User u " +
            "WHERE (:keyword IS NULL OR u.username LIKE %:keyword% OR u.nickname LIKE %:keyword% OR u.email LIKE %:keyword%) " +
            "ORDER BY u.createdAt DESC")
    Page<User> findAndSearchUser(@Param("keyword") String keyword, Pageable pageable);

    List<User> findByUsernameContainingIgnoreCase(String nickname);
}
