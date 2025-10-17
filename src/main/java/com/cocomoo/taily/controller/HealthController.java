package com.cocomoo.taily.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public String health() {
        return "OK";
    }
}
/*******************Security 설정에서 꼭 추가하세요***************************/
//Security 설정클래스의 securityFilterChain 메서드에 추가한다 
//  .requestMatchers("/api/health").permitAll() // aws health check 를 위해 
