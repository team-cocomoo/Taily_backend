package com.cocomoo.taily.security.user;

import com.cocomoo.taily.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * CustomerMemberDetails : Spring Security의 UserDetails 구현체
 * why? Spring Security가 이해할 수 있는 회원(사용자) 정보 형식
 * 예) Servelt Interface를 구현한 웹 프로그램은
 * Web Container가 service라는 단일한 방식으로 실행한다.
 * 즉 Spring Security가 표준화된 방식으로 사용자 정보를 관리하지 위해서
 * 인증(Authentication)과 인가(Authorization)를 위한 클래스
 */

@Slf4j
@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
    private final User user;

    /**
     * 사용자의 권한 정보 반환
     * Spring Security는 이 메서드를 통해 사용자의 권한을 확인
     *
     * @return 권한 목록 (ROLE_USER, ROLE_ADMIN 등)
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Member의 role을 Spring Security가 이해하는 GrantedAuthority로 변환
        // SimpleGrantedAuthority는 GrantedAuthority의 가장 간단한 구현체
        return Collections.singletonList(
                new SimpleGrantedAuthority(user.getRole().name())
        );
    }

    /**
     * 비밀번호 반환
     * Spring Security가 비밀번호 검증 시 사용
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * 사용자명(로그인 ID) 반환
     * Spring Security의 principal(주체) 식별자
     */
    @Override
    public String getUsername() {
        return user.getUsername();  // username으로 로그인
    }

    /**
     * 계정 만료 여부
     * true: 계정이 만료되지 않음
     * false: 계정이 만료됨 (로그인 불가)
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;  // 현재는 만료 기능 미구현
    }

    /**
     * 계정 잠금 여부
     * true: 계정이 잠기지 않음
     * false: 계정이 잠김 (비밀번호 5회 오류 등)
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;  // 현재는 잠금 기능 미구현
    }

    /**
     * 자격 증명(비밀번호) 만료 여부
     * true: 비밀번호가 만료되지 않음
     * false: 비밀번호가 만료됨 (변경 필요)
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // 현재는 비밀번호 만료 기능 미구현
    }

    /**
     * 계정 활성화 여부
     * true: 계정이 활성화됨
     * false: 계정이 비활성화됨 (이메일 인증 전 등)
     */
    @Override
    public boolean isEnabled() {
        return true;  // 현재는 모든 계정을 활성화 상태로 처리
    }

    /**
     * 편의 메서드: User 엔티티의 ID 가져오기
     * JWT 토큰 생성 시 사용
     */
    public Long getUserId() {
        return user.getId();
    }

    /**
     * : User 엔티티의 닉네임 가져오기
     */
    public String getNickname() {
        return user.getNickname();
    }


    public String getPublicId() {
        return user.getPublicId();
    }

}
