package com.fantion.backend.community.dto;

import com.fantion.backend.community.entity.Community;
import com.fantion.backend.member.entity.Member;
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
  public static class PostRequest {
    private Long communityId;
    private Long postId;
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
    private LocalDateTime deleteDate;
    private PostStatus status;
  }
}
