package com.fantion.backend.member.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class MemberDto {

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Request {
    @NotBlank(message = "닉네임은 공백일 수 없습니다.")
    private String nickname;
    @NotBlank(message = "주소는 공백일 수 없습니다.")
    private String address;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class Response {
    private String email;
    private String nickname;
    private String address;
    private Boolean auth;
    private String authType;
    private Integer rating;
    private String profileImage;
    private String phoneNumber;
    private Long balance;
    private LocalDateTime createDate;
  }
}
