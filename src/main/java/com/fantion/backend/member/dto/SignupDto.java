package com.fantion.backend.member.dto;

import com.fantion.backend.member.entity.Member;
import com.fantion.backend.type.MemberStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
  @Schema(description = "회원가입시 필요한 정보")
  public static class SignupRequest {

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @NotBlank(message = "이메일은 공백 일 수 없습니다.")
    @NotNull(message = "이메일은 null 일 수 없음")
    @Schema(description = "가입하려는 이메일(공백이거나 null 값일 수 없음)", example = "test@email.com")
    private String email;

    @NotBlank(message = "비밀번호는 공백 일 수 없습니다.")
    @NotNull(message = "비밀번호는 null 일 수 없음")
    @Schema(description = "비밀번호(공백이거나 null 값일 수 없음)", example = "qwe123!")
    private String password;

    @NotBlank(message = "닉네임은 공백 일 수 없습니다.")
    @NotNull(message = "닉네임은 null 일 수 없음")
    @Schema(description = "닉네임(공백이거나 null 값일 수 없음)", example = "닉네임")
    private String nickname;

    @NotBlank(message = "주소는 공백 일 수 없습니다.")
    @NotNull(message = "주소는 null 일 수 없음")
    @Schema(description = "회원의 주소(공백이거나 null 값일 수 없음)", example = "서울특별시 강남구 청담동")
    private String address;

    @NotBlank(message = "핸드폰 번호는 공백 일 수 없습니다.")
    @NotNull(message = "핸드폰 번호는 null 일 수 없음")
    @Schema(description = "회원의 핸드폰 번호(공백이거나 null 값일 수 없음)", example = "01012345678")
    private String phoneNumber;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder(toBuilder = true)
  @Schema(description = "회원가입 성공시 리턴되는 값")
  public static class SignupResponse {
    @Schema(description = "회원가입 성공여부")
    private Boolean success;
    @Schema(description = "회원가입 성공한 이메일")
    private String email;
  }

  public static Member signupInput(SignupDto.SignupRequest request, String imageUrl) {
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
