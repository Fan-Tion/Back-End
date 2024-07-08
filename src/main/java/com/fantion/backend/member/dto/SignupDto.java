package com.fantion.backend.member.dto;

import com.fantion.backend.member.entity.Member;
import com.fantion.backend.type.MemberStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.crypto.bcrypt.BCrypt;

public class SignupDto {

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder(toBuilder = true)
  public static class Request {
    private String email;
    private String password;
    private String nickname;
    private String address;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder(toBuilder = true)
  public static class Response {
    private Boolean success;
    private String email;
  }

  public static Member signupInput(Request request, String imageUrl) {
    String encPassword = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt());

    return Member.builder()
        .email(request.getEmail())
        .password(encPassword)
        .nickname(request.getNickname())
        .auth(false)
        .kakao(false)
        .naver(false)
        .address(request.getAddress())
        .totalRating(0)
        .rating(0)
        .status(MemberStatus.ACTIVE)
        .profileImage(imageUrl)
        .createDate(LocalDateTime.now())
        .build();
  }
}
