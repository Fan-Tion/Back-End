package com.fantion.backend.member.jwt;

import com.fantion.backend.exception.impl.InvalidTokenException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ObjectUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {
  private final JwtTokenProvider jwtTokenProvider;
  private final RedisTemplate<String, String> redisTemplate;
  private final AntPathMatcher pathMatcher = new AntPathMatcher();

  // 공개 엔드포인트 설정
  private static final Map<HttpMethod, List<String>> publicEndpoints = new HashMap<>();

  static { // 필요한 엔드포인트를 추가
    publicEndpoints.put(HttpMethod.POST, Arrays.asList("/members/signin", "/members/signup", "/payments/request"));
    publicEndpoints.put(HttpMethod.GET, Arrays.asList("/payments/success", "/payments/success",
        "/auction/category", "/auction/favorite-category", "/auction/search", "/auction/view/**", "/auction/list"));
  }

  // 특정 요청에 대해 토큰이 필요하지 않은 경우를 체크하는 메서드
  private boolean isPublicEndpoint(HttpServletRequest request) {
    String contextPath = request.getContextPath();
    String requestURI = request.getRequestURI();

    // 컨텍스트 패스를 제거한 URI를 구성
    String endpoint = requestURI.substring(contextPath.length());

    // 요청 메서드 가져오기
    String method = request.getMethod();
    if (method != null) {
      HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());

      List<String> endpoints = publicEndpoints.get(httpMethod);
      if (endpoints != null) {
        // 패턴 매칭
        return endpoints.stream().anyMatch(pattern -> pathMatcher.match(pattern, endpoint));
      }
    }

    return false;
  }


  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    if (isPublicEndpoint(request)) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = jwtTokenProvider.resolveToken(request);

    if (token != null && jwtTokenProvider.validateToken(token)) {
      // Redis에 해당 accessToken의 logout 여부 확인
      String isLogout = redisTemplate.opsForValue().get(token);

      // 로그아웃이 없는(되어 있지 않은) 경우 해당 토큰은 정상적으로 작동하기
      if (ObjectUtils.isEmpty(isLogout)) {
        Authentication authentication = jwtTokenProvider.getAuthentication(token);
        if (authentication != null) {
          SecurityContextHolder.getContext().setAuthentication(authentication);
        }
      } else {
        throw new InvalidTokenException();
      }
    } else {
      throw new InvalidTokenException();
    }
    filterChain.doFilter(request, response);
  }
}

