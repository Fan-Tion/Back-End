package com.fantion.backend.member.dto;

import com.fantion.backend.type.BalanceType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
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
@Schema(description = "예치금 내역 불러오기에 성공하면 리턴되는 값")
public class MyBalanceDto {

  @Schema(description = "금액")
  private Long balance;
  @Schema(description = "유형")
  private BalanceType Type;
  @Schema(description = "실행된 시간")
  private LocalDateTime createTime;
}
