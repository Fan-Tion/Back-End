package com.fantion.backend.community.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

public class ChannelRemoveDto {
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request{
        @NotBlank(message = "채널 식별자는 필수 항목입니다.")
        private Long channelId;         // 채널 식별자

    }


}
