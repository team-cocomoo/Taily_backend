package com.cocomoo.taily.repository.test;

import com.cocomoo.taily.repository.EventRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
@Slf4j
public class EventRepositoryTest {
    @Autowired
    EventRepository eventRepository;

    @Test
    void finaAll(){
        Pageable pageable = PageRequest.of(0, 10);

    }

}
