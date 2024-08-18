package com.fantion.backend.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "프로필 이미지 변경에 성공하면 리턴되는 값")
public class ProfileImageResponseDto {
  @Schema(description = "프로필 이미지 변경 성공여부")
  private Boolean success;
  @Schema(description = "새로운 프로필 이미지 URL")
  private String newProfileImageUrl;
}
