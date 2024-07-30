package com.fantion.backend.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
  @Schema(description = "회원정보 수정시 필요한 정보 ")
  public static class MemberUpdateRequest {
    @NotBlank(message = "닉네임은 공백일 수 없습니다.")
    @NotNull(message = "닉네임은 null 일 수 없음")
    @Schema(description = "닉네임(공백이거나 null 값일 수 없음)", example = "닉네임")
    private String nickname;

    @NotBlank(message = "주소는 공백 일 수 없습니다.")
    @NotNull(message = "주소는 null 일 수 없음")
    @Schema(description = "회원의 주소(공백이거나 null 값일 수 없음)", example = "서울특별시 강남구 청담동")
    private String address;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Schema(description = "회원정보 조회시 리턴되는 값 ")
  public static class MemberResponse {
    @Schema(description = "이메일")
    private String email;
    @Schema(description = "닉네임")
    private String nickname;
    @Schema(description = "주소")
    private String address;
    @Schema(description = "본인인증 여부")
    private Boolean auth;
    @Schema(description = "본인인증 타입")
    private String authType;
    @Schema(description = "사용자 평점")
    private Integer rating;
    @Schema(description = "프로필 이미지")
    private String profileImage;
    @Schema(description = "핸드폰 번호")
    private String phoneNumber;
    @Schema(description = "예치금")
    private Long balance;
    @Schema(description = "계정 생성일")
    private LocalDateTime createDate;
  }
}
