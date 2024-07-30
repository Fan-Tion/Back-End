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
@Schema(description = "평점부여시 필요한 정보")
public class RatingRequestDto {
  @Schema(description = "평점을 매기려는 판매자의 경매 식별자")
  private Long auctionId;
  @Schema(description = "해당 경매의 판매자에게 부여하고 싶은 평점")
  private Integer rating;
}
