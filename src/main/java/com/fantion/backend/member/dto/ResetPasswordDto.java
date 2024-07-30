package com.fantion.backend.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
  @Schema(description = "비밀번호 변경 메일 요청 할 때 필요한 정보")
  public static class MailRequest {

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "이메일은 공백일 수 없습니다.")
    @NotNull(message = "이메일는 null 일 수 없음")
    @Schema(description = "비밀번호를 변경하려는 이메일(공백이거나 null 값일 수 없음)", example = "test@email.com")
    private String email;

    @NotBlank(message = "핸드폰 번호는 공백일 수 없습니다.")
    @NotNull(message = "핸드폰 번호는 null 일 수 없음")
    @Schema(description = "회원의 핸드폰 번호(공백이거나 null 값일 수 없음)", example = "01012345678")
    private String phoneNumber;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Schema(description = "비밀번호 변경 요청 할 때 필요한 정보")
  public static class ChangeRequest {

    @NotBlank(message = "인증번호는 공백일 수 없습니다.")
    @Schema(description = "회원의 이메일에 전송한 랜덤한 String 값")
    private String uuid;
    @NotBlank(message = "비빌번호는 공백일 수 없습니다.")
    @Schema(description = "새롭게 변경할 비밀번호(공백이거나 null 값일 수 없음)", example = "qwe123!")
    private String newPassword;
  }
}
