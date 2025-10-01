package com.cocomoo.taily.security.user;

import com.cocomoo.taily.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * CustomerMemberDetails : Spring Security의 UserDetails 구현체
 * why? Spring Security가 이해할 수 있는 회원(사용자) 정보 형식
 * 예) Servelt Interface를 구현한 웹 프로그램은
 * Web Container가 service라는 단일한 방식으로 실행한다.
 * 즉 Spring Security가 표준화된 방식으로 사용자 정보를 관리하지 위해서
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
        return Collection.singletonList(
                new SimpleGrantedAuthority(member.getRole().name())
        )
    }

    /**
     * 비밀번호 반환
     * Spring Security가 비밀번호 검증 시 사용
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

}
