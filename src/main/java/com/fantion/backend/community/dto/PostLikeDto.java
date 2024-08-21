package com.fantion.backend.community.dto;

import lombok.*;

public class PostLikeDto {

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response{

        private boolean postLikeChk;        // 추천 여부
        private String title;               // 게시글 제목

    }
}
