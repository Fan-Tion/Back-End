package com.fantion.backend.auction.dto;

import com.fantion.backend.type.SearchType;
import lombok.Data;

@Data
public class SearchDto {
  private int page = 0;
  private SearchType searchOption;
  private String keyword;
}

