package com.cocomoo.taily.config;

import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.entity.UserRole;
import com.cocomoo.taily.entity.UserState;
import com.cocomoo.taily.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

/**
 * 서버 실행 시 관리자 계정을 자동으로 생성하는 설정 클래스
 * (UserService.register()를 재사용하지 않음)
 */
@Configuration
@RequiredArgsConstructor
public class AdminInitializer {

  private final AdminInitProps props;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Bean
  public CommandLineRunner initAdmin() {
    return args -> {
      //  1. 설정 비활성화 시 종료
      if (!props.isEnable()) {
        System.out.println("️ 관리자 자동 생성 비활성화됨 (admin.init.enable=false)");
        return;
      }

      // 🔍 2. 이미 존재하면 스킵
      if (userRepository.findByUsername(props.getUsername()).isPresent()) {
        System.out.println(" 관리자 계정 이미 존재함: " + props.getUsername());
        return;
      }

      // 3. 관리자 유저 생성
      User admin = User.builder()
          .publicId(UUID.randomUUID().toString())
          .username(props.getUsername())
          .nickname(props.getNickname())
          .password(passwordEncoder.encode(props.getPassword()))
          .tel("000-0000-0000")
          .email(props.getEmail())
          .address("관리자 주소")
          .introduction("시스템 자동 생성 관리자 계정입니다.")
          .role(UserRole.ROLE_ADMIN)
          .state(UserState.ACTIVE)
          .build();

      userRepository.save(admin);
      System.out.println("✅ 관리자 계정 생성 완료: " + props.getUsername());
    };
  }
}
