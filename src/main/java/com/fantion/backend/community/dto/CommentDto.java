package com.fantion.backend.community.dto;

import com.fantion.backend.community.entity.Comment;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class CommentDto {

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class CommentRequest{
    @NotBlank(message = "댓글은 공백일 수 없습니다.")
    @NotNull(message = "댓글은 null 일 수 없음")
    private String content;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  public static class CommentResponse{
    private Long commentId;
    private String nickname;
    private String content;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime createDate;
  }

  public static CommentDto.CommentResponse toResponse(Comment comment) {
    return CommentResponse.builder()
        .commentId(comment.getCommentId())
        .nickname(comment.getMember().getNickname())
        .content(comment.getContent())
        .createDate(comment.getCreateDate())
        .build();
  }
}
