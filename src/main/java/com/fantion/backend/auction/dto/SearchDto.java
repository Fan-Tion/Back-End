package com.fantion.backend.auction.dto;

import lombok.Data;

@Data
public class SearchDto {
  private int page;
  private String category;
  private String keyword;
}

