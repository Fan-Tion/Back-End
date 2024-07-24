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
import org.springframework.security.crypto.bcrypt.BCrypt;

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
    @NotBlank(message = "핸드폰 번호는 공백일 수 없습니다.")
    private String phoneNumber;
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
    String encPassword = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt());

    return Member.builder()
        .email(request.getEmail())
        .password(encPassword)
        .nickname(request.getNickname())
        .auth(false)
        .isKakao(false)
        .isNaver(false)
        .address(request.getAddress())
        .phoneNumber(request.getPhoneNumber())
        .totalRating(0)
        .rating(0)
        .status(MemberStatus.ACTIVE)
        .profileImage(imageUrl)
        .createDate(LocalDateTime.now())
        .build();
  }
}
