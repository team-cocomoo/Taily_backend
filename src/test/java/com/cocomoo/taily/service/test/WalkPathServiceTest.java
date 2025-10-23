package com.cocomoo.taily.service.test;

import com.cocomoo.taily.security.config.SecurityConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@SpringBootTest
@Import(SecurityConfig.class)
@Transactional
public class WalkPathServiceTest {

}

