package com.fantion.backend.auction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "카테고리 정보")
public class CategoryDto {
  @Schema(description = "카테고리 이름")
  String title;
  @Schema(description = "카테고리 url")
  String category;
}
