package com.fantion.backend.member.jwt;

import com.fantion.backend.member.dto.TokenDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

  private final Key key;

  // application.yml secret 값 가져와서 key에 저장
  public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    this.key = Keys.hmacShaKeyFor(keyBytes);
  }

  public TokenDto createTokens(String email, Long memberId, String nickname) {

    // AccessToken 클레임 설정
    Claims accessTokenClaims = Jwts.claims().setSubject(email);
    accessTokenClaims.put("memberId", memberId);
    accessTokenClaims.put("nickname", nickname);
    accessTokenClaims.put("roles", Collections.singletonList("ROLE_USER"));

    // RefreshToken 클레임 설정
    Claims refreshTokenClaims = Jwts.claims().setSubject(email);
    accessTokenClaims.put("roles", Collections.singletonList("ROLE_USER"));

    long now = (new Date()).getTime();
    Date accessTokenExpiresIn = new Date(now + 7200000);
    Date refreshTokenExpiresIn = new Date(now + 86400000);

    String accessToken = Jwts.builder()
        .setClaims(accessTokenClaims)
        .setIssuedAt(new Date(now))
        .setExpiration(accessTokenExpiresIn)
        .signWith(SignatureAlgorithm.HS256, key)
        .compact();

    String refreshToken = Jwts.builder()
        .setClaims(refreshTokenClaims)
        .setIssuedAt(new Date(now))
        .setExpiration(refreshTokenExpiresIn)
        .signWith(SignatureAlgorithm.HS256, key)
        .compact();

    TokenDto tokens = TokenDto.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .build();

    return tokens;
  }

  public String reissue(String email) {

    Claims claims = Jwts.claims().setSubject(email);
    claims.put("roles", Collections.singletonList("ROLE_USER"));

    long now = (new Date()).getTime();
    Date accessTokenExpiresIn = new Date(now + 3600000);

    String accessToken = Jwts.builder()
        .setClaims(claims)
        .setIssuedAt(new Date(now))
        .setExpiration(accessTokenExpiresIn)
        .signWith(SignatureAlgorithm.HS256, key)
        .compact();

    return accessToken;
  }

  public Authentication getAuthentication(String token) {
    Claims claims = getClaimsFromToken(token);
    String userId = claims.getSubject();
    Collection<? extends GrantedAuthority> authorities = getRolesFromToken(token)
        .stream()
        .map(SimpleGrantedAuthority::new)
        .collect(Collectors.toList());

    return new UsernamePasswordAuthenticationToken(userId, null, authorities);
  }

  public String resolveToken(HttpServletRequest request) {
    final String BEARER = "Bearer ";

    String token = request.getHeader("Authorization");
    if (token != null && token.startsWith(BEARER)) {
      token = token.substring(BEARER.length()); // "Bearer " 이후의 토큰 값만 추출
    }
    return token;
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parser().setSigningKey(key).parseClaimsJws(token);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public Claims getClaimsFromToken(String token) {
    return Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
  }

  public List<String> getRolesFromToken(String token) {
    Claims claims = getClaimsFromToken(token);
    return claims.get("roles", List.class);
  }
}
