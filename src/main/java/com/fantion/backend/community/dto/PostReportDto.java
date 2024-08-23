package com.fantion.backend.community.dto;

import lombok.*;

public class PostReportDto {
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Request{
        private Long postId;            // 게시글 식별자
        private String description;     // 신고 내용

    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response{
        private String title;           // 게시글 제목

    }
}
