package com.fantion.backend.member.entity;

import com.fantion.backend.type.MemberStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Member {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long memberId;

  private String email;
  private String password;
  private String nickname;
  private Boolean auth;
  private Boolean kakao;
  private Boolean naver;
  private String address;
  private Integer totalRating;
  private Integer ratingCnt;
  private Integer rating;

  @Enumerated(EnumType.STRING)
  private MemberStatus status;

  private String profileImage;
  private LocalDateTime createDate;
}
