package com.fantion.backend.community.entity;

import com.fantion.backend.member.entity.Member;
import com.fantion.backend.type.PostStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "post")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Post {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long postId;

  @ManyToOne
  @JoinColumn(name = "community_id")
  private Community community;

  @ManyToOne
  @JoinColumn(name = "member_id")
  private Member member;

  private String title;
  private String content;
  private Integer likeCnt;
  private Integer viewCnt;
  private LocalDateTime createDate;
  private LocalDateTime deleteDate;

  @Enumerated(EnumType.STRING)
  private PostStatus status;
}
