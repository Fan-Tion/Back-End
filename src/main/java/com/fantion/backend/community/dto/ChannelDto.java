package com.fantion.backend.community.dto;

import com.fantion.backend.community.entity.Channel;
import com.fantion.backend.type.ChannelStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

public class ChannelDto {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request{
        @NotBlank(message = "채널 제목은 필수 항목입니다.")
        private String title;           // 채널 제목

        @NotBlank(message = "채널 설명은 필수 항목입니다.")
        private String description;     // 채널 설명

    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response{

        private Long channelId;                 // 채널 식별자
        private String organizer;               // 채널 주최자
        private String title;                   // 채널 제목
        private String description;             // 채널 설명
        private String image;                   // 채널 이미지
        private ChannelStatus status;         // 채널 상태
        private LocalDateTime createDate;       // 채널 생성일
    }

    public static ChannelDto.Response response(Channel channel) {
        return Response.builder()
                .channelId(channel.getChannelId())
                .organizer(channel.getOrganizer().getNickname())
                .title(channel.getTitle())
                .description(channel.getDescription())
                .image(channel.getImage())
                .status(channel.getStatus())
                .createDate(channel.getCreateDate())
                .build();
    }

}
