package com.cocomoo.taily.config;

import com.cocomoo.taily.entity.TableType;
import com.cocomoo.taily.entity.User;
import com.cocomoo.taily.entity.UserRole;
import com.cocomoo.taily.entity.UserState;
import com.cocomoo.taily.repository.DummyRepository;
import com.cocomoo.taily.repository.TableTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final DummyRepository dummyRepository;
    private final TableTypeRepository tableTypeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (dummyRepository.count() == 0) { // 유저가 없을 때만 생성

            // Optional import 확인: java.util.Optional
            Optional<TableType> optionalTableType = tableTypeRepository.findById(1L);
        }

    }
}