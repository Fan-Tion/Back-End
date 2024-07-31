package com.fantion.backend.auction.dto;

import com.fantion.backend.type.CategoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class AuctionDto {

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Schema(description = "경매 생성, 수정할 때 필요한 정보")
  public static class AuctionRequest {

    @NotBlank(message = "제목은 꼭 입력해야 합니다.")
    @Schema(description = "경매 제목", example = "타이틀")
    private String title;

    @NotNull(message = "카테고리는 꼭 입력해야 합니다.")
    @Schema(description = "경매 카테고리", example = "ALBUM")
    private CategoryType category;

    @NotNull(message = "경매 방식은 꼭 입력해야 합니다.")
    @Schema(description = "경매 타입(공개 true, 비공개 false)", example = "true")
    private boolean auctionType;

    @NotBlank(message = "설명은 꼭 입력해야 합니다.")
    @Schema(description = "경매 물품에 대한 설명")
    private String description;

    @NotNull(message = "시작 입찰가는 꼭 입력해야 합니다.")
    @Schema(description = "경매 입찰 시작가", example = "10000")
    private Long currentBidPrice;

    @NotNull(message = "즉시 구매가는 꼭 입력해야 합니다.")
    @Schema(description = "즉시 구매가(입찰 시작가 보다 작을 수 없음)", example = "50000")
    private Long buyNowPrice;

    @NotNull(message = "종료일은 꼭 입력해야 합니다.")
    @Schema(description = "종료일")
    private LocalDate endDate;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Schema(description = "경매에 대한 리턴되는 데이터 값")
  public static class AuctionResponse {
    @Schema(description = "경매 식별자")
    private Long auctionId;
    @Schema(description = "경매 제목")
    private String title;
    @Schema(description = "경매 생성자 닉네임")
    private String auctionUserNickname;
    @Schema(description = "카테고리")
    private CategoryType category;
    @Schema(description = "경매 타입(공개 true, 비공개 false")
    private boolean auctionType;
    @Schema(description = "경매 이미지")
    private List<String> auctionImage;
    @Schema(description = "경매 본문")
    private String description;
    @Schema(description = "현재 입찰가")
    private Long currentBidPrice;
    @Schema(description = "현재 입찰자")
    private String currentBidder;
    @Schema(description = "즉시 구매가")
    private Long buyNowPrice;
    @Schema(description = "좋아요(찜) 된 횟수")
    private Long favoriteCnt;
    @Schema(description = "경매 생성일")
    private LocalDate createDate;
    @Schema(description = "경매 종료일")
    private LocalDateTime endDate;
    @Schema(description = "경매 상태")
    private boolean status;
    @Schema(description = "판매자 평점")
    private Integer rating;
  }
}
