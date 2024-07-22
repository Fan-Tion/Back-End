package com.fantion.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fantion.backend.member.configuration.NaverConfiguration;
import com.fantion.backend.member.configuration.NaverLoginClient;
import com.fantion.backend.member.service.impl.MemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.view.RedirectView;

@ExtendWith(MockitoExtension.class)
public class NaverServiceTest {

  @Mock
  private NaverLoginClient naverLoginClient;

  @Mock
  private NaverConfiguration naverConfiguration;

  @InjectMocks
  private MemberServiceImpl memberService;

  @Value("${naver.clientId}")
  private String clientId;

  @Value("${naver.clientSecret}")
  private String clientSecret;

  @BeforeEach
  void setUp() {
    // 설정값 모킹
    when(naverConfiguration.getClientId()).thenReturn(clientId);
    when(naverConfiguration.getState()).thenReturn("STRING_STATE");
    when(naverConfiguration.getRedirectUri()).thenReturn(
        "http://localhost:8080/members/naver/signin");
  }

//  @Test
//  void testNaverRequest() {
//    // given
//    String expectedResponse = "response";
//    when(naverLoginClient.naverRequest("code", naverConfiguration.getClientId(),
//        naverConfiguration.getState(), naverConfiguration.getRedirectUri()))
//        .thenReturn(ResponseEntity.ok(expectedResponse));
//
//    // when
//    RedirectView response = memberService.naverRequest();
//
//    // then
//    assertEquals(expectedResponse, response);
//    verify(naverLoginClient).naverRequest("code", naverConfiguration.getClientId(),
//        naverConfiguration.getState(), naverConfiguration.getRedirectUri());
//  }
}
