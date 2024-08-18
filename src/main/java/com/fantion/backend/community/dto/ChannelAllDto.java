package com.fantion.backend.community.dto;

import lombok.*;

import java.util.List;

public class ChannelAllDto {
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response{

        private char character;                             // 초성
        private List<ChannelDto.Response> channelList;      // 초성에 맞는 채널 리스트

    }

}
