package com.fantion.backend.member.configuration;

import com.fantion.backend.member.dto.NaverMemberDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "naverProfileClient", url = "${naver.profile-base-url}")
public interface NaverProfileClient {

  @GetMapping(value = "${naver.profile-endpoint}")
  ResponseEntity<NaverMemberDto> getProfile(@RequestHeader("Authorization") String accessToken);
}
