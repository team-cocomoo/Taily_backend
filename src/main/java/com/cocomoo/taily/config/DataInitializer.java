package com.cocomoo.taily.config;

import com.cocomoo.taily.entity.TableType;
import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.entity.UserRole;
import com.cocomoo.taily.entity.UserState;
import com.cocomoo.taily.repository.DummyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final DummyRepository dummyRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (dummyRepository.count() == 0) { // 유저가 없을 때만 생성
            User dummy = User.builder()
                    .publicId(UUID.randomUUID().toString())
                    .username("testuser")
                    .nickname("테스트유저")
                    .password(passwordEncoder.encode("1234"))  // security 적용되면 반드시 encoder 사용!
                    .tel("010-0000-0000")
                    .email("test@test.com")
                    .address("서울시")
                    .role(UserRole.ROLE_USER)
                    .state(UserState.ACTIVE)
                    .tableType(TableType.builder().id(1L).build())
                    .build();

            dummyRepository.save(dummy);
        }
    }
}
