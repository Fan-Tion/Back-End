package com.fantion.backend.community.dto;

import com.fantion.backend.community.entity.Post;
import com.fantion.backend.type.PostStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotBlank(message = "제목은 공백일 수 없습니다.")
    @NotNull(message = "제목은 null 일 수 없음")
    private String title;
    private String content;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class PostUpdateRequest {

    @NotBlank(message = "제목은 공백일 수 없습니다.")
    @NotNull(message = "제목은 null 일 수 없음")
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
    private String channelImage;
    private String channelDescription;
    private String nickname;
    private String title;
    private String content;
    private Integer likeCnt;
    private Integer viewCnt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createDate;

    private PostStatus status;
  }

  public static PostDto.PostResponse toResponse(Post post) {
    return PostDto.PostResponse.builder()
        .postId(post.getPostId())
        .channelName(post.getChannel().getTitle())
        .channelImage(post.getChannel().getImage())
        .channelDescription(post.getChannel().getDescription())
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
