package com.fantion.backend.payment.dto;

import com.fantion.backend.type.BalanceType;
import com.fasterxml.jackson.annotation.JsonFormat;
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
@Builder(toBuilder = true)
@Schema(description = "토스 결제 취소를 할 때 필요한 정보")
public class CancelDto {
  @Schema(description = "취소 금액")
  private Long balance;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS", timezone = "Asia/Seoul")
  @Schema(description = "토스 결제 승인 날짜와 시간")
  private LocalDateTime createTime;
  @Schema(description = "토스 결제 취소 이유")
  private String cancelReason;
}
