package com.fantion.backend.auction.dto;

import com.fantion.backend.type.SearchType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Valid
public class SearchDto {
  @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
  private int page = 0;
  @NotNull(message = "카테고리를 입력해주세요")
  private SearchType category;
  private String keyword;
}

