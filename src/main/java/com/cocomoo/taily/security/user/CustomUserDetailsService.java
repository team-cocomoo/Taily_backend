package com.cocomoo.taily.security.user;

import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 CustomerMemberDetailService -> Spring Security의 UserDetailService의 구현체
 역할 : Spring Filter Proxy에서 사용자 인증(로그인 체크)시 DB와 연동해 인증 여부를 담당

 */
@RequiredArgsConstructor
@Service
@Slf4j
@Transactional(readOnly = true)
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Spring Security가 인증 과정에서 자동으로 호출하는 메서드
     *
     * 동장 과정
     * 1. 사용자가 로그인 폼에 username과 password 입력
     * 2. Spring Security Filter Proxy가 username으로 아래 메서드 호출
     * 3. Repository를 이용해 사용자 정보 조회 후 UserDetails(사용자 정보)를 반환
     * 4. Spring Security Filter Proxy가 입력한 비밀 번호의 UserDetails 비밀번호를 비교
     * 5. 일치하면 인증 성공, Authentication 객체 생성 (SecurityContext에 저장되는 정보)
     *                                                  여러 컨트롤러들이 접근해 사용자 정보를 이용한다
     *
     * @param username 로그인 시 입력한 사용자명
     * @return UserDetails 사용자 상세 정보
     * @throws UsernameNotFoundException 사용자를 찾을 수 없을 때
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("=== Spring Security 인증 시작 ===");
        log.info("로그인 시도: username={}", username);
        // 주의점 : findById가 아니고 findByUsername 메서드를 호출
        User findUser = userRepository.findByUsername(username).orElseThrow(()->{
            log.error("인증 실패 - 존재하지 않는 사용자 : {}", username);
            return new UsernameNotFoundException("아이디에 해당하는 회원이 존재하지 않습니다"+username);
        });
        log.info("사용자 닉네임 확인 username={}, nickname={}, role={}", findUser.getUsername(),findUser.getNickname(),findUser.getRole());
        // Security가 이해하는 사용자 타입인 UserDetails 인터페이스의 구현체를 반환
        return new CustomUserDetails(findUser);
    }

    /**
     * JWT 토큰 검증 시 사용할 수 있는 추가 메서드
     * userId로 사용자 조회
     *
     * @param userId 회원 ID (PK)
     * @return UserDetails 사용자 상세 정보
     */
    public UserDetails loadUserByMemberId(Long userId) {
        log.info("=== JWT 검증을 위한 사용자 조회: userId={} ===", userId);
        User user = userRepository.findById(userId).orElseThrow(()->{
            log.error("사용자를 찾을 수 없음");
            return new UsernameNotFoundException("사용자를 찾을 수 없음 : ID="+userId);
        });

        log.info("사용자 조회 성공: username={}, role={}",
                user.getUsername(), user.getRole());

        return new CustomUserDetails(user);
    }
}