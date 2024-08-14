package com.fantion.backend.auction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class AuctionUpdateDto {
  private MultipartFile file;
  private String url;
}
