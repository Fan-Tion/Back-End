package com.fantion.backend.member.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.UriComponentsBuilder;

@Data
@Configuration
@ConfigurationProperties(prefix = "naver")
public class NaverConfiguration {
  private String clientId;
  private String clientSecret;
  private String redirectUri;
  private String state;
}
