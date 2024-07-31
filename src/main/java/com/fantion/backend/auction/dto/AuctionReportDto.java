package com.fantion.backend.auction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class AuctionReportDto {

  @Getter
  @Setter
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  @Schema(description = "경매 신고를 위한 정보")
  public static class AuctionReportRequest {
    @Schema(description = "신고 내용")
    private String description;
  }

  @Getter
  @Setter
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  @Schema(description = "경매 신고 리턴되는 값")
  public static class AuctionReportResponse {
    @Schema(description = "신고 당한 경매 제목")
    private String title;
  }
}
