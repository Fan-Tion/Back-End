package com.fantion.backend.payment.component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "payment")
public class PaymentComponent {
  private static final String BASIC_DELIMITER = ":";
  private static final String AUTH_HEADER_PREFIX = "Basic ";

  private String secretKey;

  public String createAuthorizationHeader() {
    Base64.Encoder encoder = Base64.getEncoder();
    String auth = secretKey + BASIC_DELIMITER;
    byte[] encodedAuth = encoder.encode(auth.getBytes(StandardCharsets.UTF_8));
    return AUTH_HEADER_PREFIX + new String(encodedAuth);
  }
}

