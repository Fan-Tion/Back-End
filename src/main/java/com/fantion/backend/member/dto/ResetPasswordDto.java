package com.fantion.backend.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ResetPasswordDto {

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class MailRequest {

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "이메일은 공백일 수 없습니다.")
    private String email;
    @NotBlank(message = "핸드폰 번호는 공백일 수 없습니다.")
    private String phoneNumber;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class ChangeRequest {

    @NotBlank(message = "인증번호는 공백일 수 없습니다.")
    private String uuid;
    @NotBlank(message = "비빌번호는 공백일 수 없습니다.")
    private String newPassword;
  }
}
