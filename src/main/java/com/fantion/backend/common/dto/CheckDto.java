package com.fantion.backend.common.dto;

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
@Schema(description = "API 성공여부를 알려주는 정보")
public class CheckDto {
  @Schema(description = "API 성공여부")
  private Boolean success;
}
