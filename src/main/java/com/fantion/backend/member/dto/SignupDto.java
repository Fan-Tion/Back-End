package com.fantion.backend.member.dto;

import com.fantion.backend.member.entity.Member;
import com.fantion.backend.type.MemberStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class SignupDto {

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder(toBuilder = true)
  public static class Request {

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "이메일은 공백일 수 없습니다.")
    private String email;
    @NotBlank(message = "비밀번호는 공백일 수 없습니다.")
    private String password;
    @NotBlank(message = "닉네임은 공백일 수 없습니다.")
    private String nickname;
    @NotBlank(message = "주소는 공백일 수 없습니다.")
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

  public static Member signupInput(SignupDto.Request request, String imageUrl) {

    return Member.builder()
        .email(request.getEmail())
        .password(request.getPassword())
        .nickname(request.getNickname())
        .auth(false)
        .isKakao(false)
        .isNaver(false)
        .address(request.getAddress())
        .totalRating(0)
        .rating(0)
        .status(MemberStatus.ACTIVE)
        .profileImage(imageUrl)
        .createDate(LocalDateTime.now())
        .build();
  }
}
