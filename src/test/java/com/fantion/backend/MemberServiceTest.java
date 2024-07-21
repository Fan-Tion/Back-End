package com.fantion.backend;

import com.fantion.backend.exception.ErrorCode;
import com.fantion.backend.exception.impl.CustomException;
import com.fantion.backend.member.dto.SigninDto;
import com.fantion.backend.member.dto.SignupDto;
import com.fantion.backend.member.dto.TokenDto;
import com.fantion.backend.member.entity.Member;
import com.fantion.backend.member.jwt.JwtTokenProvider;
import com.fantion.backend.member.repository.MemberRepository;
import com.fantion.backend.member.service.impl.MemberServiceImpl;
import com.fantion.backend.type.MemberStatus;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

  @InjectMocks
  private MemberServiceImpl memberService;

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private JwtTokenProvider jwtTokenProvider;

  @Mock
  private RedisTemplate<String, String> redisTemplate;

  @Mock
  private ValueOperations<String, String> valueOperations;

  @Mock
  private HttpServletRequest httpServletRequest;

  @Test
  void testSignup() {
    // given
    SignupDto.Request request = SignupDto.Request.builder()
        .email("test@email.com")
        .password("1234")
        .nickname("테스트")
        .address("테스트 주소")
        .phoneNumber("01000000000")
        .build();
    when(memberRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
    when(memberRepository.findByLinkedEmail(request.getEmail())).thenReturn(Optional.empty());
    when(memberRepository.findByNickname(request.getNickname())).thenReturn(Optional.empty());
    SignupDto.Response expectedResponse = SignupDto.Response.builder()
        .success(true)
        .email("test@email.com")
        .build();

    // when
    SignupDto.Response response = memberService.signup(request, null);

    // then
    assertEquals(expectedResponse.getSuccess(), response.getSuccess());
    assertEquals(expectedResponse.getEmail(), response.getEmail());
    verify(memberRepository, times(1)).save(any());
  }

  @Test
  void testSignup_InvalidEmail() {
    // given
    SignupDto.Request request = SignupDto.Request.builder()
        .email("test")
        .password("1234")
        .nickname("테스트")
        .address("테스트 주소")
        .phoneNumber("01000000000")
        .build();

    // when, then
    assertThrows(CustomException.class, () -> memberService.signup(request, null));
  }

  @Test
  void testSignin_memberNotFound() {
    // given
    SigninDto signinDto = new SigninDto("nonexistent@email.com", "password");

    when(memberRepository.findByEmail(signinDto.getEmail())).thenReturn(Optional.empty());

    // when / then
    assertThrows(CustomException.class, () -> memberService.signin(signinDto));
    verify(memberRepository, times(1)).findByEmail(signinDto.getEmail());
  }

  @Test
  void testSignin_memberSuspended() {
    // given
    SigninDto signinDto = new SigninDto("suspended@email.com", "password");
    Member suspendedMember = Member.builder()
        .email("suspended@email.com")
        .password("password")
        .status(MemberStatus.SUSPENDED)
        .build();

    when(memberRepository.findByEmail(signinDto.getEmail())).thenReturn(
        Optional.of(suspendedMember));

    // when / then
    assertThrows(CustomException.class, () -> memberService.signin(signinDto));
    verify(memberRepository, times(1)).findByEmail(signinDto.getEmail());
  }

  @Test
  void testSignin_memberWithdrawn() {
    // given
    SigninDto signinDto = new SigninDto("withdrawn@email.com", "password");
    Member withdrawnMember = Member.builder()
        .email("withdrawn@email.com")
        .password("password")
        .status(MemberStatus.WITHDRAWN)
        .build();

    when(memberRepository.findByEmail(signinDto.getEmail())).thenReturn(
        Optional.of(withdrawnMember));

    // when / then
    assertThrows(CustomException.class, () -> memberService.signin(signinDto));
    verify(memberRepository, times(1)).findByEmail(signinDto.getEmail());
  }

  @Test
  void testSignin_validCredentials() {
    // given
    SigninDto signinDto = new SigninDto("valid@email.com", "password");
    Member activeMember = Member.builder()
        .email("valid@email.com")
        .password("password")
        .status(MemberStatus.ACTIVE)
        .build();

    String expectedAccessToken = "mocked-access-token";
    String expectedRefreshToken = "mocked-refresh-token";
    TokenDto.Local expectedTokens = new TokenDto.Local(expectedAccessToken, expectedRefreshToken);

    when(memberRepository.findByEmail(signinDto.getEmail())).thenReturn(Optional.of(activeMember));
    when(jwtTokenProvider.createTokens(activeMember.getEmail(), activeMember.getMemberId(),
        activeMember.getNickname()))
        .thenReturn(expectedTokens);

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    // when
    TokenDto.Local actualTokens = memberService.signin(signinDto);

    // then
    assertEquals(expectedTokens, actualTokens);
    verify(memberRepository, times(1)).findByEmail(signinDto.getEmail());
    verify(valueOperations, times(1))
        .set(eq("RefreshToken: " + activeMember.getEmail()), eq(expectedRefreshToken), anyLong(),
            any(TimeUnit.class));
  }

  @Test
  void testSignin_invalidPassword() {
    // given
    SigninDto signinDto = new SigninDto("valid@email.com", "wrongpassword");
    Member activeMember = Member.builder()
        .email("valid@email.com")
        .password("password")
        .status(MemberStatus.ACTIVE)
        .build();

    when(memberRepository.findByEmail(signinDto.getEmail())).thenReturn(Optional.of(activeMember));

    // when / then
    assertThrows(CustomException.class, () -> memberService.signin(signinDto));
    verify(memberRepository, times(1)).findByEmail(signinDto.getEmail());
  }
}


