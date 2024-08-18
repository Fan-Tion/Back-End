package com.fantion.backend.community.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

public class ChannelEditDto {

  @Getter
  @Setter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Request {

    @NotBlank(message = "채널 식별자는 필수 항목입니다.")
    private Long channelId;         // 채널 식별자

    @NotBlank(message = "채널 제목은 필수 항목입니다.")
    private String title;           // 채널 제목

    @NotBlank(message = "채널 설명은 필수 항목입니다.")
    private String description;     // 채널 설명

  }
}
