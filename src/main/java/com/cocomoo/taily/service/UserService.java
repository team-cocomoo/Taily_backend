package com.cocomoo.taily.service;

import com.cocomoo.taily.dto.User.*;
import com.cocomoo.taily.dto.admin.AdminUserResponseDto;
import com.cocomoo.taily.dto.myPage.UserProfileResponseDto;
import com.cocomoo.taily.entity.*;
import com.cocomoo.taily.repository.ImageRepository;
import com.cocomoo.taily.repository.UserRepository;
import com.cocomoo.taily.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageService imageService;
    private final ImageRepository imageRepository;
    private final JwtUtil jwtUtil;

    /**
     * 1. 회원가입 - publicId는 UUID로 생성
     */
    @Transactional
    public UserResponseDto register(UserCreateRequestDto requestDto) {
        log.info("회원가입 시도: username={}", requestDto.getUsername());

        if (userRepository.existsByUsername(requestDto.getUsername())) {
            log.warn("회원가입 실패 - 중복된 username: {}", requestDto.getUsername());
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        String uuid = UUID.randomUUID().toString();

        User user = User.builder()
                .publicId(uuid)
                .username(requestDto.getUsername())
                .nickname(requestDto.getNickname())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .tel(requestDto.getTel())
                .email(requestDto.getEmail())
                .address(requestDto.getAddress())
                .introduction(requestDto.getIntroduction())
                .role(UserRole.ROLE_USER)
                .state(UserState.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        log.info("회원가입 성공: id={}, username={}, publicId={}",
                savedUser.getId(), savedUser.getUsername(), savedUser.getPublicId());

        return UserResponseDto.from(savedUser);
    }

    /**
     * 2. username으로 회원 조회 - JWT 토큰 발급 시 사용
     */
    public UserProfileResponseDto findByUsername(String username) {
        log.debug("회원 조회: username={}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("회원 조회 실패: username={}", username);
                    return new IllegalArgumentException("존재하지 않는 회원입니다.");
                });

        return UserProfileResponseDto.from(user);
    }

    /**
     * 3. ID(pk)로 회원 조회 - JWT 토큰 검증 후 사용
     */
    public UserResponseDto findById(Long userId) {
        log.debug("회원 조회: id={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("회원 조회 실패: id={}", userId);
                    return new IllegalArgumentException("존재하지 않는 회원입니다.");
                });

        return UserResponseDto.from(user);
    }


    /**
     * 4. 현재 로그인한 회원 정보 조회
     */
    public UserProfileResponseDto getMyInfo(String username) {
        return findByUsername(username);
    }

    /**
     * 5. 비밀번호 검증 - 로그인 시 사용
     */
    public boolean validatePassword(String username, String rawPassword) {
        log.debug("비밀번호 검증: username={}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        boolean isValid = passwordEncoder.matches(rawPassword, user.getPassword());

        if (isValid) {
            log.info("비밀번호 검증 성공: username={}", username);
        } else {
            log.warn("비밀번호 검증 실패: username={}", username);
        }

        return isValid;
    }

    /**
     * 6. 회원 정보 수정 - 인증된 사용자만 가능
     */
    @Transactional
    public UserResponseDto updateMember(
            String username,
            String newUsername,
            String newNickname,
            String newPassword,
            String newTel,
            String newEmail,
            String newAddress,
            String newIntroduction,
            UserState newState
    ) {
        log.info("회원 정보 수정: username={}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        String encodedPassword = null;
        if (newPassword != null && !newPassword.isBlank()) {
            encodedPassword = passwordEncoder.encode(newPassword);
        }

        // updateInfo 메서드 호출
        user.updateInfo(
                newUsername,
                newNickname,
                encodedPassword,
                newTel,
                newEmail,
                newAddress,
                newIntroduction,
                newState
        );

        log.info("회원 정보 수정 완료: username={}", username);
        return UserResponseDto.from(user);
    }

    /**
     * 7. 회원 Entity 조회 - 내부용
     */
    public User getUserEntity(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
    }

    /**
     * 로그인 메서드
     * @param requestDto
     * @return
     */
    public UserLoginResponseDto login(UserLoginRequestDto requestDto) {
        User user = userRepository.findByUsername(requestDto.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }

        // JWT 토큰 생성 (만료 1시간)
        String token = jwtUtil.createJwt(user, 1000L * 60 * 60);

        return new UserLoginResponseDto(
                user.getUsername(),
                user.getNickname(),
                user.getRole().name(),
                token
        );
    }

    @Transactional
    public UserProfileResponseDto updateMyProfileByPublicId(String publicId, UserUpdateRequestDto requestDto) {
        log.info("회원 정보 수정: publicId={}", publicId);

        User user = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 🔹 비밀번호가 입력된 경우에만 암호화 후 변경
        String encodedPassword = null;
        if (requestDto.getPassword() != null && !requestDto.getPassword().isBlank()) {
            encodedPassword = passwordEncoder.encode(requestDto.getPassword());
        }

        // UserState 값이 null일 경우 기존 state 값 유지
        UserState newState = null;
        if (requestDto.getState() != null) {
            try {
                newState = UserState.valueOf(requestDto.getState());
            } catch (IllegalArgumentException e) {
                log.warn("잘못된 상태 값입니다. state={}", requestDto.getState());
                newState = user.getState(); // 기존 상태 유지
            }
        } else {
            newState = user.getState(); // null이면 기존 상태 유지
        }

        // 🔹 user 엔티티에 업데이트 적용
        user.updateInfo(
                requestDto.getUsername(),
                requestDto.getNickname(),
                encodedPassword != null ? encodedPassword : user.getPassword(), // 기존 비밀번호 유지
                requestDto.getTel(),
                requestDto.getEmail(),
                requestDto.getAddress(),
                requestDto.getIntroduction(),
                newState
        );

        log.info("회원 정보 수정 완료: publicId={}", publicId);
        return UserProfileResponseDto.from(user);
    }


    @Transactional
    public void deleteMyAccount(String username) {
        log.info("회원 탈퇴 시도: username={}", username);

        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

            if (user.getState() == UserState.WITHDRAW) {
                throw new IllegalStateException("이미 탈퇴한 회원입니다.");
            }

            // Soft Delete (상태만 변경)
            user.updateInfo(
                    user.getUsername(),
                    user.getNickname(),
                    user.getPassword(),
                    user.getTel(),
                    user.getEmail(),
                    user.getAddress(),
                    user.getIntroduction(),
                    UserState.WITHDRAW
            );

            log.info("회원 탈퇴 완료: username={}, state={}", username, user.getState());

        } catch (IllegalArgumentException e) {
            log.error("회원 탈퇴 실패 - 존재하지 않는 사용자: {}", username, e);
            throw e; // → 컨트롤러에서 GlobalExceptionHandler에 의해 처리됨
        } catch (IllegalStateException e) {
            log.warn("회원 탈퇴 중복 요청: username={}, message={}", username, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("회원 탈퇴 처리 중 예상치 못한 오류 발생: username={}", username, e);
            throw new RuntimeException("회원 탈퇴 중 오류가 발생했습니다.");
        }
    }


    // 관리자 로그인
    public User findAdminByUsername(String username) {
        log.info("관리자 조회: username={}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("관리자 조회 실패: username={}", username);
                    return new IllegalArgumentException("존재하지 않는 관리자 계정입니다.");
                });
        if (user.getRole() != UserRole.ROLE_ADMIN) {
            throw new IllegalArgumentException("관리자 권한이 없습니다.");
        }
        return user;
    }

    // 관리자가 하는 회원 정보 조회
    @Transactional
    public AdminUserResponseDto findUserInfoById(Long id) {
        log.info("관리자 조회: id={}", id);
        User user = userRepository.findById(id).orElseThrow(() -> {
            log.error("관리자 회원 조회 실패: id={}", id);
            return new IllegalArgumentException("존재하지 않는 회원입니다.");
        });

        log.info("관리자 회원 상세 조회 성공: userId={}", user.getId());

//        TableType user

        // 작성자 프로필 조회 + url 완성
        String imagePath = imageService.getImages(1L, user.getId(), null)
                .stream()
                .map(Image::getFilePath)
                .findFirst()
                .orElse(null);

        return AdminUserResponseDto.from(user, imagePath);
    }

    public boolean isUsernameDuplicate(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean isNicknameDuplicate(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    public boolean isEmailDuplicate(String email) {
        return userRepository.existsByEmail(email);
    }

}
