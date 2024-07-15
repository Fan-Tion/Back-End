package com.fantion.backend.auction.dto;

import com.fantion.backend.type.SearchType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchDto {
  private int page = 0;
  private SearchType searchOption;
  private String keyword;
}

