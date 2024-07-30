package com.fantion.backend.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "로그인 시 필요한 정보")
public class SigninDto {

  @Email(message = "이메일 형식이 올바르지 않습니다.")
  @NotBlank(message = "이메일은 공백일 수 없습니다.")
  @Schema(description = "로그인하려는 이메일(공백이거나 null 값일 수 없음)", example = "test@email.com")
  private String email;

  @NotBlank(message = "비밀번호는 공백일 수 없습니다.")
  @Schema(description = "비밀번호(공백이거나 null 값일 수 없음)", example = "qwe123!")
  private String password;
}
