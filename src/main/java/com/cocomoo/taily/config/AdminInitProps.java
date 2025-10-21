package com.cocomoo.taily.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 관리자 초기화 설정 정보를 application-local.properties 에서 읽어오는 클래스
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "admin.init")
public class AdminInitProps {
  private boolean enable;
  private String username;
  private String password;
  private String nickname;
  private String email;
}
