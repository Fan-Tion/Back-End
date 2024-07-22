package com.fantion.backend.member.configuration;

import com.fantion.backend.member.dto.NaverLinkDto;
import com.fantion.backend.member.dto.TokenDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "naverLoginClient", url = "${naver.login-base-uri}")
public interface NaverLoginClient {

  @GetMapping(value = "${naver.token-endpoint}")
  ResponseEntity<TokenDto.Naver> getToken(
      @RequestParam("grant_type") String grantType,
      @RequestParam("client_id") String clientId,
      @RequestParam("client_secret") String clientSecret,
      @RequestParam("code") String code,
      @RequestParam("state") String state
  );

  @GetMapping(value = "${naver.token-endpoint}")
  ResponseEntity<NaverLinkDto> unLink(
      @RequestParam("client_id") String clientId,
      @RequestParam("client_secret") String clientSecret,
      @RequestParam("access_token") String naverAccessToken,
      @RequestParam("grant_type") String grantType
  );
}
