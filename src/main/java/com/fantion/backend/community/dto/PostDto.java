package com.fantion.backend.community.dto;

import com.fantion.backend.community.entity.Post;
import com.fantion.backend.type.PostStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class PostDto {

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class PostCreateRequest {

    private Long postId;
    private String title;
    private String content;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class PostUpdateRequest {

    private String title;
    private String content;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class PostResponse {

    private Long postId;
    private String channelName;
    private String nickname;
    private String title;
    private String content;
    private Integer likeCnt;
    private Integer viewCnt;
    private LocalDateTime createDate;
    private PostStatus status;
  }

  public static PostDto.PostResponse toResponse(Post post) {
    return PostDto.PostResponse.builder()
        .postId(post.getPostId())
        .channelName(post.getChannel().getTitle())
        .nickname(post.getMember().getNickname())
        .title(post.getTitle())
        .content(post.getContent())
        .likeCnt(post.getLikeCnt())
        .viewCnt(post.getViewCnt())
        .createDate(post.getCreateDate())
        .status(post.getStatus())
        .build();
  }
}
